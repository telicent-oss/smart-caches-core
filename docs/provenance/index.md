# Provenance Capture - SPIKE

The platform's events carry provenance/audit headers (see [Kafka headers](../event-sources/kafka.md) and
`TelicentHeaders`) describing how a message reached a Smart Cache: `Request-ID`, `Input-Request-ID`, `Exec-Path`,
`Distribution-Id` and `Security-Label`. These are normally consumed and discarded during ingest. Provenance capture
records them **as data**, so that "where did this come from, when, via which path, under which label?" becomes a
queryable question rather than something lost in the logs.

This module provides the shared, dependency-free building blocks; the RDF-aware components (the
[Fuseki-Kafka connector](#in-the-knowledge-graph) and the [catalogue updater](#in-the-catalogue)) turn them into
PROV-O / DCAT triples.

## Building blocks

- **`ProvenanceRecord`** — an immutable snapshot of the provenance headers on an `Event`, plus an ingest timestamp.
  It has no RDF dependency.

  ```java
  ProvenanceRecord prov = ProvenanceRecord.fromEvent(event);
  prov.distributionId();   // "dist-1"
  prov.execPathSteps();    // ["adapter", "mapper", "projector"]
  prov.hasProvenance();    // true if any provenance header was present
  ```

- **`ProvenanceVocabulary`** — the shared PROV-O / DCAT term IRIs (as plain strings) and IRI-minting helpers, so every
  component emits the same vocabulary. The model follows W3C PROV-O aligned with DCAT 3.

## Enabling it

Provenance capture is **opt-in** and disabled by default. Enable it with either:

- Environment variable: `TELICENT_PROVENANCE_CAPTURE=true`
- System property (takes precedence): `-Dtelicent.provenance.capture=true`

The same switch governs both the Fuseki-Kafka connector and the catalogue updater, so a single setting turns provenance
on across the platform. When disabled, behaviour and output are unchanged.

## What it looks like

### In the knowledge graph

Suppose a `knowledge` topic event arrives with these headers:

| Header | Value |
|--------|-------|
| `Distribution-Id` | `dist-1` |
| `Request-ID` | `req-1` |
| `Input-Request-ID` | `in-0` |
| `Exec-Path` | `adapter,mapper` |
| `Security-Label` | `clearance=secret` |

With capture enabled, the data is applied as normal and, **in the same transaction**, a provenance record is written
into the per-distribution provenance named graph `<http://telicent.io/provenance/dist-1>`:

```turtle
@prefix prov:    <http://www.w3.org/ns/prov#> .
@prefix dcat:    <http://www.w3.org/ns/dcat#> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix tprov:   <http://telicent.io/provenance#> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .

# graph <http://telicent.io/provenance/dist-1>

<http://telicent.io/provenance#activity/req-1>
    a                     prov:Activity ;
    prov:generatedAtTime  "2026-06-12T10:00:00Z"^^xsd:dateTime ;
    prov:used             <http://telicent.io/provenance#distribution/dist-1> ;
    tprov:requestId       "req-1" ;
    tprov:inputRequestId  "in-0" ;
    tprov:execPath        "adapter,mapper" ;
    tprov:securityLabel   "clearance=secret" .

<http://telicent.io/provenance#distribution/dist-1>
    a                     dcat:Distribution ;
    dcterms:identifier    "dist-1" .
```

The ingested data itself stays in its normal graph(s); provenance lives in its own named graph, so existing queries are
unaffected unless they explicitly ask for it.

### In the catalogue

When the catalogue updater sees a `Distribution-Id` plus a `Data-Source-Reference` header (e.g.
`kafka://ingest/source-A`) and capture is enabled, the catalogue entry is enriched so a user can trace the distribution
back to its source:

```turtle
@prefix dcat:    <http://www.w3.org/ns/dcat#> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix prov:    <http://www.w3.org/ns/prov#> .
@prefix tcat:    <http://telicent.io/catalog/Distribution#> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .

tcat:dist-1
    rdf:type            dcat:Distribution, prov:Entity ;
    dcterms:available   "2026-06-12T10:00:00Z"^^xsd:dateTime ;
    dcterms:source      "kafka://ingest/source-A" ;
    dcat:accessService  _:src_dist_1 ;
    prov:wasDerivedFrom _:src_dist_1 .

_:src_dist_1
    rdf:type            dcat:DataService ;
    dcterms:identifier  "kafka://ingest/source-A" .
```

With capture disabled (the default), or when no `Data-Source-Reference` is present, the catalogue output is exactly the
legacy form (`dcat:Distribution` + `dcterms:available` only).

## Querying it

Get everything recorded for one distribution:

```sparql
SELECT ?p ?o
WHERE { GRAPH <http://telicent.io/provenance/dist-1> { ?s ?p ?o } }
```

When, and via which pipeline, was a distribution's data ingested?

```sparql
PREFIX prov:  <http://www.w3.org/ns/prov#>
PREFIX tprov: <http://telicent.io/provenance#>

SELECT ?ingestedAt ?execPath
WHERE {
    GRAPH ?g {
        ?activity a prov:Activity ;
                  prov:generatedAtTime ?ingestedAt ;
                  prov:used <http://telicent.io/provenance#distribution/dist-1> .
        OPTIONAL { ?activity tprov:execPath ?execPath }
    }
}
```

What was ingested after a given time, across all distributions?

```sparql
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>

SELECT ?g ?activity ?ingestedAt
WHERE {
    GRAPH ?g { ?activity a prov:Activity ; prov:generatedAtTime ?ingestedAt }
    FILTER (?ingestedAt > "2026-06-01T00:00:00Z"^^xsd:dateTime)
}
```

## Notes and caveats

- **Best-effort.** A failure to record provenance is logged and swallowed — it never fails or rolls back the ingest of
  the actual data.
- **Union default graph.** If a dataset is configured with `tdb2:unionDefaultGraph true`, the contents of all named
  graphs (including provenance) appear in the default-graph view, so an unscoped query will also see provenance. Where
  strict separation matters, exclude the provenance graph from the union or always query named graphs explicitly.
- **Security labelling.** Provenance triples are currently written without an explicit ABAC label, so their visibility
  follows the dataset default. A deliberate policy (e.g. label the provenance graph for auditors) is the recommended
  next step.
- **`Exec-Path` delimiter.** `ProvenanceRecord.execPathSteps()` assumes a comma delimiter; confirm against the canonical
  `telicent-lib` provenance format before relying on it.
