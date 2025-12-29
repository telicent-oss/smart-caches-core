window.BENCHMARK_DATA = {
  "lastUpdate": 1766997335030,
  "repoUrl": "https://github.com/telicent-oss/smart-caches-core",
  "entries": {
    "Run Auth Engine Benchmark": [
      {
        "commit": {
          "author": {
            "name": "Paul Gallagher",
            "username": "TelicentPaul",
            "email": "132362215+TelicentPaul@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "ba715f4b8567cc50d4755ef313da74ab56ffc9cd",
          "message": "Merge pull request #242 from telicent-oss/core_1096_add_benchmarking\n\n[CORE-1096] Adding initial benchmarking\n\nIt's not impacting anything else and everyone is busy. I want to see how it runs prior to leaving",
          "timestamp": "2025-12-12T13:52:57Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/ba715f4b8567cc50d4755ef313da74ab56ffc9cd"
        },
        "date": 1765548243649,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 173.17044734183605,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.8476625100835,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.11388985671314,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.32474004211744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 177.0021785524986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.7131226917164,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 173.8941549940568,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 174.2770078196849,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.40661860931135,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.3980252524472,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.51366133977402,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.92300487875772,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.6352502222846,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 175.88081731343894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 176.2832342234996,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 176.25152322683144,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 174.6838466020353,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 175.02499349515975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.553447884199938,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.667075284839052,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.653286093427633,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.210336641184046,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.707748962718999,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.510865986137244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.152006261707443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.503037200014869,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.510438066494318,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.257471753122783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.5631109129772955,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.361567510475661,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.7145222168333785,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.841883649351656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.0805629607696288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.920254576785336,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.803339888048464,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 2.9917589510653317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul Gallagher",
            "username": "TelicentPaul",
            "email": "132362215+TelicentPaul@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "ba715f4b8567cc50d4755ef313da74ab56ffc9cd",
          "message": "Merge pull request #242 from telicent-oss/core_1096_add_benchmarking\n\n[CORE-1096] Adding initial benchmarking\n\nIt's not impacting anything else and everyone is busy. I want to see how it runs prior to leaving",
          "timestamp": "2025-12-12T13:52:57Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/ba715f4b8567cc50d4755ef313da74ab56ffc9cd"
        },
        "date": 1765787020498,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 172.84795557549197,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 175.0887665377525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 173.31907646918194,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 173.88752648549763,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.05331425352588,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.89926214822384,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 175.36909175705026,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 172.98771385898186,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.19813057870562,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.454258977453,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 171.74080298462718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 176.1033712729255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.42306486528133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.06731010841332,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 176.12538178987356,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 173.83650902781466,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.2141747846862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.09725038197246,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 7.754243058392207,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.20681402963911,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.534586649794738,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.280529859945847,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.472680132799733,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.4749977908049985,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.237658190955786,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.49694561120712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.702408713247184,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.33124519371616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 6.899346867375779,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.611374292854236,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.647593115957932,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.943341859782718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.118235463790566,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.870912546913084,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.831749641336632,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.068502339284352,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul Gallagher",
            "username": "TelicentPaul",
            "email": "132362215+TelicentPaul@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "543810563099b8e11c9694e266a4812e7d4e13e7",
          "message": "Merge pull request #244 from telicent-oss/minor/gh_actions_fix\n\n[CORE-1096] Extending benchmarking - fixing typo in command",
          "timestamp": "2025-12-15T14:02:57Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/543810563099b8e11c9694e266a4812e7d4e13e7"
        },
        "date": 1765808321299,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 173.76933720442736,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.8270162254747,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.33272725979208,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.3330418311763,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 173.02024569610379,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 173.9600662433133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 173.00910909016034,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 175.84800123660375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.17427790336689,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 172.79410125968144,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.35699233976072,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.132656182914,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.56408759346286,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 173.28483800836509,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.70228922373718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 173.2943456518859,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 175.35634789171553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.05445217084815,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.148859888620217,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.238480879580086,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.755766350457224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.241342807187836,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.580576373003761,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.693016018551222,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.4376605279168,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.271171932628451,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.744266564640126,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.100037708659311,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.7628372597948685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.671242048997982,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.953593983015223,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.504176985475908,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.096237549754719,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.980893982822304,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.94324359124611,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.098226933217385,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Rob Vesse",
            "username": "rvesse",
            "email": "rob.vesse@telicent.io"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "6dd8f8b17f96bea29a195727c240d15bdbd0ea4e",
          "message": "Merge pull request #248 from telicent-oss/CORE-907\n\nRaw header value overloads (CORE-907)",
          "timestamp": "2025-12-18T15:09:19Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/6dd8f8b17f96bea29a195727c240d15bdbd0ea4e"
        },
        "date": 1766391773683,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 173.37720267748253,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.74374566355698,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.3214417043469,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.25838749660178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 175.17345200954912,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.90606466728738,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 174.99339693001122,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.50903091844194,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 176.24425671655712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 175.06844906005085,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.3860230276103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.96081350443973,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 175.87027353057923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 173.2697511294886,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.4725693932451,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 173.27706340634538,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.84661553848343,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.96834334516782,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.272726974883856,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.489415017114638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.6993063660159375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.420632611983402,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.122808853957987,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.604356631339197,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.139934301093566,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.5247273498744915,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.598446261401571,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.296854730674827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.522185153150767,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.651679509347434,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.830863868419897,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.421355074878819,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.0586257037371003,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 6.082393477908621,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.729012209086962,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 2.9791915862236538,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Rob Vesse",
            "username": "rvesse",
            "email": "rob.vesse@telicent.io"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "6dd8f8b17f96bea29a195727c240d15bdbd0ea4e",
          "message": "Merge pull request #248 from telicent-oss/CORE-907\n\nRaw header value overloads (CORE-907)",
          "timestamp": "2025-12-18T15:09:19Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/6dd8f8b17f96bea29a195727c240d15bdbd0ea4e"
        },
        "date": 1766996586993,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.63731863605173,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.13584781005116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.15959724105423,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.11310551098123,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 173.99215531220722,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.06212092761228,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 175.7516631596672,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.60095800798578,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.58537460148082,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.2046207682718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.15245260687206,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.92431981015025,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 173.40121230190866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 173.31560166807944,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 172.21097707479908,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 174.55062288182762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 174.64398660682565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 173.16423088349458,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.259834104882852,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.538165875379812,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.717744102433399,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.238431306968867,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 6.732136610377543,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.743127608764072,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.322766758262917,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.591680559924411,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.509505400063897,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.219082500661667,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.377717484924853,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.7355718985332915,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 6.005966653211258,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.697297759269302,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.1128102044116783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.964089514054021,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.8128277996713145,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0591318101860367,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      }
    ],
    "Run JWT Parsing Benchmark": [
      {
        "commit": {
          "author": {
            "name": "Paul Gallagher",
            "username": "TelicentPaul",
            "email": "132362215+TelicentPaul@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "543810563099b8e11c9694e266a4812e7d4e13e7",
          "message": "Merge pull request #244 from telicent-oss/minor/gh_actions_fix\n\n[CORE-1096] Extending benchmarking - fixing typo in command",
          "timestamp": "2025-12-15T14:02:57Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/543810563099b8e11c9694e266a4812e7d4e13e7"
        },
        "date": 1765809070008,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.166361732742414,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9794136984432598,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8754587966094773,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3608067657162244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3632466370702465,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09156023482866574,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.270775267927368,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07171847386962095,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017812221550759927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.154163453460801,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9081617457664253,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.869785873108216,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3358748429422103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.36410066681826014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0918989910872593,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.272109583113071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07269824280144335,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017910835668621606,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.1117187909365183,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.054442075793489655,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01867709350228803,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.025579573075893296,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007521403541615855,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019313364719436483,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005614383052617945,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015183452038977931,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00039480452967969074,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.0203008316914152,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.017006557450881858,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010663303080160736,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012557740831305387,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005752691960081288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.001787501153109038,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004523420624882921,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.001404250982216355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00038911617956376317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000953194734891046,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000011052230180188727,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00000966422821515469,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010149024212292348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00000921269820761648,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010251765895524306,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009258474882585902,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009376522239263371,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009909815829594191,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009214760290490225,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010497123872974227,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009896092322779289,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009805253060937614,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010621917204416495,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009643708900426283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010226568030429093,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009641589150369616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009237035513177443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Rob Vesse",
            "username": "rvesse",
            "email": "rob.vesse@telicent.io"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "6dd8f8b17f96bea29a195727c240d15bdbd0ea4e",
          "message": "Merge pull request #248 from telicent-oss/CORE-907\n\nRaw header value overloads (CORE-907)",
          "timestamp": "2025-12-18T15:09:19Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/6dd8f8b17f96bea29a195727c240d15bdbd0ea4e"
        },
        "date": 1766392521356,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.089069279428012,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9272231447517005,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8765329724013421,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3673537222654233,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3589659726680706,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09056479105917839,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.26410548820616414,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07032255555950562,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017763942257958493,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.084266155185222,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.905557103352032,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8405633989869973,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3563176239178492,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3550227206535614,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09034591802060972,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2712046321107714,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07178851978056348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017671198761087827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.11106138418386909,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.05460343273030595,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01833359214865062,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.02474276164712195,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007497843413673724,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019167585514818116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005639973088678053,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015179658417906927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0004019115304736864,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020418003872082074,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.017261318870955594,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010663410335905543,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012640990808058883,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005730773831649082,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0017659433692825328,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004543379611506212,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0013993993708422366,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003798382929330095,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009109718162030402,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010430370662876006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010421953965041851,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010233415601263924,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000011669085760814333,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009802228962871586,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010447067362722104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010183612668709193,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009734844808693658,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009945496024811386,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010658160779427275,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009215635949349033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009435559295711595,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001103778101535399,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010349985261108384,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009571975084095814,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009534170478661594,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010476481325100558,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Rob Vesse",
            "username": "rvesse",
            "email": "rob.vesse@telicent.io"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "6dd8f8b17f96bea29a195727c240d15bdbd0ea4e",
          "message": "Merge pull request #248 from telicent-oss/CORE-907\n\nRaw header value overloads (CORE-907)",
          "timestamp": "2025-12-18T15:09:19Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/6dd8f8b17f96bea29a195727c240d15bdbd0ea4e"
        },
        "date": 1766997334663,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.145947052195401,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9413346915784553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8667543070689442,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.318840662705111,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.35599173620577723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09036589130306262,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.267207555681056,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07147762603822647,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01787755631665388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.166120622528853,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9107609647172916,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8659743693511117,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3552294258784714,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.362153096300896,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.08986612496929355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2727965560058463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07094476021705676,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017835252818401724,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.11143650136881178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.05442852835608349,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018358705224943823,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.025631950313626434,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007506971396889156,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.001919418157923136,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005614229351093741,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.00151633741782045,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0004025015542731273,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020107167538378025,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.016955822656541986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010690143238292519,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012651333808944617,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005742681886288965,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0017769625323678744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004528206383973195,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.00141105149123103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00038886788006285916,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009282597488132922,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000008929204048838975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010458083947752775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009964010140202146,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00000990581685168308,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009482665583068523,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010333222622146589,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010998671601912721,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00000982548777874513,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010654147362587119,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009838654953898131,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010303223506403096,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010419541537461283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010377769399716789,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010348959030438003,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009034100482393048,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001106722844495265,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010171100856091775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}