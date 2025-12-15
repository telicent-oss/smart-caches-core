window.BENCHMARK_DATA = {
  "lastUpdate": 1765787020825,
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
      }
    ]
  }
}