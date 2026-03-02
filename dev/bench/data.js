window.BENCHMARK_DATA = {
  "lastUpdate": 1772440996057,
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
        "date": 1767601416719,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 169.76596119546917,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 170.0161191216092,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 168.56053924333233,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 168.13705223008455,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 172.75957557918696,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 167.7775493395186,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 166.70956278217815,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 164.0918504540061,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 166.36760466382046,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 162.47077060547628,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 165.32590090540864,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 166.21931038066083,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 163.45707204655164,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 168.31134535327584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 165.96433318717823,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 168.53069268724502,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 169.20835762434632,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 168.59168046360526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.292291954304895,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.41556376378212,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.681228856906339,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.374953248833998,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.276892400331251,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.667036102581271,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.500389161855676,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.485378160770102,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.569047256665197,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.284622874669939,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.7160906571119225,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.567789630703633,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 6.176275213773499,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.762731161364949,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.078795205726281,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.692848062811615,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.83852684822301,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0958461092703895,
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
          "id": "82e83bd15e0be65b76d126c6b2b17a792fa266c4",
          "message": "Merge pull request #249 from telicent-oss/dependabot/maven/patches-ecdfe60131\n\nBump the patches group with 5 updates",
          "timestamp": "2026-01-05T13:16:11Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/82e83bd15e0be65b76d126c6b2b17a792fa266c4"
        },
        "date": 1768206194108,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 171.90618876314426,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 175.15368492169824,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.47858847869244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 175.52554832440515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.96232777178443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.05738051186432,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 171.35647678002496,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 175.33024179002342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.37872229322195,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 175.78082319981993,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.91122338130168,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.6303442102071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 175.47205219494245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 175.2970348313913,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.07639668970063,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 174.4392647530037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 175.16105184608318,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.55991889775973,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.198648961418607,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.614581225369951,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.466671111936828,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.317599758246862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.14581497460089,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.582828170385046,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.264036674028224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.513815553739159,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.5324107170204035,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.243322584261989,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.560099946729737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.695379355958981,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 6.108819246612734,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.911066408485565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.1017456803745427,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 6.09043765592161,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.697469110345757,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0634068257134706,
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
          "id": "8a8132f96782b47796aa424510f912b0b7eb6eea",
          "message": "Merge pull request #250 from telicent-oss/dependabot/maven/patches-f4a517cab4\n\nBump the patches group with 7 updates",
          "timestamp": "2026-01-13T07:43:26Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/8a8132f96782b47796aa424510f912b0b7eb6eea"
        },
        "date": 1768811027993,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 157.15711752087282,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 157.7647912569201,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 157.73392163657905,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 156.0233546939681,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 156.0296175875439,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 155.1295907354644,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 152.92905052070745,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 154.03247632175066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 156.82791009585463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 154.83144034733218,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 155.58146767355862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 158.40743062437883,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 158.39804322661584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 155.18534660352506,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 157.9832181204817,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 158.753173369919,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 155.14493340966538,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 156.40148552424006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.325720637507903,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.530796965135769,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.686052186107078,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.054091263592374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.610253796565634,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.695152917810637,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.494817460865926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.524430608018086,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.389191326489685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.309416434705414,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.291718813658835,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.6130937123843605,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.855061055691893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.755480593966762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.0110414675775123,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.69445781496187,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.808664457229061,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0064033001191683,
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
          "id": "001a06e1842fccc6261eec6148aca28dc92060be",
          "message": "Merge pull request #255 from telicent-oss/dependabot/docker/docker/telicent/telicent-java21-1.2.39\n\nBump telicent/telicent-java21 from 1.2.35 to 1.2.39 in /docker",
          "timestamp": "2026-01-23T08:47:06Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/001a06e1842fccc6261eec6148aca28dc92060be"
        },
        "date": 1769415786632,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 172.42572952608094,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 172.28139415093685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 172.26559201588253,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 173.38632594004076,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.79032315736157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 172.00055461069482,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 175.37925688388418,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 175.0278188266838,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 175.11855724780452,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.2553662639675,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 172.53604264938065,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.125340548018,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.46472021805707,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.01959279389825,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 173.97436792479868,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 172.9580319802443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.31327667180855,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 170.8534459465242,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.288718494689352,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.320519987185546,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.625280969224455,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.355033864980367,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.508479637127216,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.589548031693,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.244670350968818,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.4608586575258276,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.666370155505353,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 7.826826985423688,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.360754187655836,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.429570114255202,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.895899040962163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.597819051370012,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.05906417270823,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.83344761465816,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.823091823283142,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.133341941007098,
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
          "id": "ba4e2839eeca5d584839dce4dbebd5714724ba07",
          "message": "Merge pull request #261 from telicent-oss/dependabot/docker/docker/telicent/telicent-java21-1.2.40\n\nBump telicent/telicent-java21 from 1.2.39 to 1.2.40 in /docker",
          "timestamp": "2026-01-29T07:48:22Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/ba4e2839eeca5d584839dce4dbebd5714724ba07"
        },
        "date": 1770021359890,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.60766859260534,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 170.9588895875703,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 172.08469416597296,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 175.31161620888543,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 170.4252072649815,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 172.97501524502522,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 171.19501721620716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 170.58336671758346,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 170.5500964517407,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 173.90096655686557,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 171.5857951871132,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 172.90421860637704,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 172.78977006986028,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 171.32507673784133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.39707106516803,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 173.2019502385966,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 172.9875649442717,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 172.6800546946517,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.185243840658789,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.254913813809095,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.65986550939745,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.349014194812668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.578889795333066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.525373601315264,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.19735283921195,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.611562210268995,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.559338020136134,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.165299882348709,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.525790579721712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.648760906785682,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.824515005585165,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.882494788935512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.101634987721325,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.572677131926867,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.603637128468837,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0685978569992582,
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
          "id": "b38d827107303c4640808c16b8ec6caffa5e3099",
          "message": "Merge pull request #264 from telicent-oss/release/0.35.0\n\nComplete Release 0.35.0",
          "timestamp": "2026-02-04T12:32:53Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/b38d827107303c4640808c16b8ec6caffa5e3099"
        },
        "date": 1770625985950,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 173.85577636631766,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.66211027174933,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.251274945219,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 173.89114873180978,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 175.27221958684459,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.7932982186446,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 175.18835024992103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.82105597772892,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.25984908589226,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 175.95362563428483,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 176.15167188360002,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.65003325805682,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 175.69169708133526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.33114296840716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.61585538275986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 175.4298327974896,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 174.3752757325901,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 173.53874638872097,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.192637722183562,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.659963474054737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.656227450523935,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.076492194027088,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.610013692489957,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.677532469633424,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.335496080528316,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.379831003412079,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.684858828336206,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.37310558141535,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.453327203532512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.7238687203681575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 6.11839637193863,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.919239630812224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.05583684819602,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.892283997760041,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.982641885591224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0625779749730833,
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
          "id": "8bc90e92b5bbaee3b926d07b7941d63810eb237c",
          "message": "Merge pull request #265 from telicent-oss/dependabot/maven/patches-b71ceb3b82\n\nBump the patches group with 7 updates",
          "timestamp": "2026-02-10T08:01:54Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/8bc90e92b5bbaee3b926d07b7941d63810eb237c"
        },
        "date": 1771230686791,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.58344594999633,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 173.97900808352512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.08071352655867,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 173.12702285476334,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.64348462432014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 172.9956272996019,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 174.08855812395643,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 174.5392972029857,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 175.0482582439413,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.06519856590927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.0349042483062,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.48011115045307,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.1980363650319,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 172.2983132432161,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.09748611231058,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 174.3183856829283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.65140894581288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.63033367798684,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 7.648392320985224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.528852363935376,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.598454148140192,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 7.545949623451196,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.477694632174659,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.433688101018401,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.291952378451516,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.548826391996499,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.549845909760597,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 7.452066379551157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.585534232460796,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.506486846823217,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.795918705666173,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.800812154320156,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.0874200024766387,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 6.094275517560842,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.928740641797848,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 2.871397378438133,
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
          "id": "d10667e90b957397f727ce27dc34fc1fe901cb26",
          "message": "Merge pull request #267 from telicent-oss/dependabot/maven/patches-8785ebac60\n\nBump ch.qos.logback:logback-classic from 1.5.28 to 1.5.32 in the patches group",
          "timestamp": "2026-02-23T08:27:20Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/d10667e90b957397f727ce27dc34fc1fe901cb26"
        },
        "date": 1771835530379,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 175.51605228198886,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 176.43419503234435,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 176.31188389764606,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.50907622077557,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 175.8081245596547,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 176.4299644767358,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 176.58757615509768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 174.12315563886818,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 175.61443637241896,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 176.36893788354078,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 176.3115741635125,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 175.29518996204888,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.30042089387138,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 177.25380289432297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 177.7054224742327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 176.8277386291142,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 176.13354768375194,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 176.3586001434808,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.285860120596166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.455797302143703,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.43372052810427,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.250531994817559,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.545362447701992,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.411629629911405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.23409414818659,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.539901741990533,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.535104719337836,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.306702528460796,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.55917457754048,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.519411107414227,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.612753741076219,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.728714541099077,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.0324050250076717,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.619006709198133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.748008947137484,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.0119671625945825,
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
          "id": "bc157621a11d494585c60193ee720c6e4acfef64",
          "message": "Merge pull request #273 from telicent-oss/dependabot/docker/docker/telicent/telicent-java21-1.2.41\n\nBump telicent/telicent-java21 from 1.2.40 to 1.2.41 in /docker",
          "timestamp": "2026-02-27T08:51:00Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/bc157621a11d494585c60193ee720c6e4acfef64"
        },
        "date": 1772440248178,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 174.04540164431276,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.1733056183531,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 173.91631208134575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 174.0688033705584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 175.00697311213906,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 174.1287678840452,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 175.32610468229936,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 175.53684536586545,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 175.07734919789738,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 175.6731885722664,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 174.468900730696,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 174.81985187585673,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 168.09267327454634,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 174.30563678307743,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 175.98128986161257,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 174.27855828713317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 173.59749021671138,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureDenied ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 174.46756325406082,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.378702050287291,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.560231677447104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.49081264662439,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 8.244142955844495,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 7.555585792086957,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 5.709661872349313,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 8.20527173115459,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 7.468501254405732,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"1\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 5.490410635030385,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"1\"} )",
            "value": 8.38032708108483,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"5\"} )",
            "value": 7.678868442358436,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"1\",\"userRolesCount\":\"20\"} )",
            "value": 5.6876326284369565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"1\"} )",
            "value": 5.889881031712104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"5\"} )",
            "value": 4.84213610485681,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"5\",\"userRolesCount\":\"20\"} )",
            "value": 3.0907572011271727,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"1\"} )",
            "value": 5.68212809851696,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"5\"} )",
            "value": 4.826444551479373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.TelicentAuthorizationEngineBenchmark.authorizeSecureSuccess ( {\"policyPermissionsCount\":\"5\",\"userPermissionsCount\":\"20\",\"userRolesCount\":\"20\"} )",
            "value": 3.1156984820293707,
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
        "date": 1767602165600,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.029302161947961,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.812973382266608,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.814895520385382,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.245377485965225,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.34129270728074934,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.08431935808110826,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.255161518570641,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.06655776443309322,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01706183437379514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.179574794106665,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.8530745938983793,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.804895330532841,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.2975466207817683,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.33646816710864624,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.08613425505679458,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.24572921388666905,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.06803775753019824,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.016979197478916262,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.10932681774819249,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.054343521119253314,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018597836346155784,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.02587888477159031,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007482645635701018,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019269539810963178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005579450562175154,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015180635187021798,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003985232021196029,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020345697373058226,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.01693697561816373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010527744080252897,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012580979214694758,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005822700590367558,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.001733272785095725,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004527623989436745,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0013873939408330482,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00038047877374528633,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010775864079512783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.0000095860476002755,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009805122290236533,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009729599897477708,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001002932969480063,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000008550133047627433,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009518143124336656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009843739709623014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00001006496780592877,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010573967012898134,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009964208536343048,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009246055639455655,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009980346579387291,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009292823621036652,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00001133236407960893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000882561888535958,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010116819962786879,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000008679677942989113,
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
          "id": "82e83bd15e0be65b76d126c6b2b17a792fa266c4",
          "message": "Merge pull request #249 from telicent-oss/dependabot/maven/patches-ecdfe60131\n\nBump the patches group with 5 updates",
          "timestamp": "2026-01-05T13:16:11Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/82e83bd15e0be65b76d126c6b2b17a792fa266c4"
        },
        "date": 1768206942607,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.163787385324084,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.892479532650092,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8662535451891819,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3057466742751234,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.35659111864375664,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09005095849890427,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.26615977674169694,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07079473502414271,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01786874281941718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.180807753167245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.908046328389543,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8499368509574825,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3523989880103222,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3592426885362208,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09046077076370507,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.27123838999673244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07105179901388996,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018132723195217075,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.11061787787128877,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.05437472338796436,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018361894225959487,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.02604864733348341,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007554072778538023,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019289805212918583,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.0055739314455923155,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015168505737366846,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00039548432018724514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020088412135752014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.016920598758531902,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010640865737636656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012501280775540944,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005772165366311224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0017551839696905865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004710811030572033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0014133253439207515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003826744609283166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000940551001686211,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001058881283800081,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010713028711810477,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009625294286814839,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009834836445358229,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000011510942508398572,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010801581918963042,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009909177060966445,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010524528740507987,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000991734745723824,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009367383262563982,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009191874184706266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009371799902320043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001010884469093337,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010120911074045684,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010375235437381735,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009853167345004402,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010327933706400674,
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
          "id": "8a8132f96782b47796aa424510f912b0b7eb6eea",
          "message": "Merge pull request #250 from telicent-oss/dependabot/maven/patches-f4a517cab4\n\nBump the patches group with 7 updates",
          "timestamp": "2026-01-13T07:43:26Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/8a8132f96782b47796aa424510f912b0b7eb6eea"
        },
        "date": 1768811775475,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 5.992414963039904,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.5958165879388493,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.7487017555729037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.2050434289439846,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.31963050900496387,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.08098306846833347,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.24185870721913377,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.06333326141715526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01569466713895269,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 5.9127094671190505,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.6571889948796703,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.7635958840121491,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.1956474747368113,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3226975466391309,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.08097138885567638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.23982614146193745,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0629512674002813,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.015749696923609488,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.10790776489507223,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.053691597823782744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018477127672231187,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.025202280113156006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00743825320428706,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0018669280521568083,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005554600630241529,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015068865150356903,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003915757074703526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.019907324458339256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.017182846969870307,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010604721617162072,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012450218438813387,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005776226118591163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0017476447099312076,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004518902365477877,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0014122348850897542,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00037933001046726886,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009795508526010603,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009977301133022011,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009757480359741951,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009983444067422466,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009974185424606204,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00001040285634934978,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000947543005831342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010189030921313555,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009271983432602593,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009686480738836248,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009917001453662393,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009499152690802034,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009842902638759456,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009861257410175699,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009529898982056932,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010048915432258656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000011053833218206502,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009667417301735685,
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
          "id": "001a06e1842fccc6261eec6148aca28dc92060be",
          "message": "Merge pull request #255 from telicent-oss/dependabot/docker/docker/telicent/telicent-java21-1.2.39\n\nBump telicent/telicent-java21 from 1.2.35 to 1.2.39 in /docker",
          "timestamp": "2026-01-23T08:47:06Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/001a06e1842fccc6261eec6148aca28dc92060be"
        },
        "date": 1769416535839,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.128302493130927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9600831325469557,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8650228047328771,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3120575080394297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.35833205562446346,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09028578385748812,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2717731520168388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07169049541510267,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018121113854837546,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.092057693366728,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9698084862420613,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.873171085140191,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3385363794696281,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.36150357276104383,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0907229432797092,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.27547956398835477,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07192874606502878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01808722192886672,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.11104810123200304,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.054540070861452995,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018427686650948127,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.025844469033938045,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007399784106196227,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0018905427105110587,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.00560065488949281,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015113258498321842,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00039459176270642185,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020278434848846404,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.016899408360959878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010577098840996963,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012562341125266626,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.0057767214109880035,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0017670982208173303,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.0045260677910260606,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0014127763730935002,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00038763530947079404,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009645064975816448,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009627322805152625,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009573605911253149,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009324979702946641,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010118945309401087,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009259193259958554,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009872580534400242,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010283786209456967,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010934820163815668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000878718663091985,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009228438918970768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010449144861790467,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000998609206795246,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000008792599398367085,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010204424259800088,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009262501469782896,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010057437546648702,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00000935028475780371,
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
          "id": "ba4e2839eeca5d584839dce4dbebd5714724ba07",
          "message": "Merge pull request #261 from telicent-oss/dependabot/docker/docker/telicent/telicent-java21-1.2.40\n\nBump telicent/telicent-java21 from 1.2.39 to 1.2.40 in /docker",
          "timestamp": "2026-01-29T07:48:22Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/ba4e2839eeca5d584839dce4dbebd5714724ba07"
        },
        "date": 1770022107841,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.075103308698882,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9298186244483917,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.863157106395167,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3331817404515545,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.368550992565375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09108977598170762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2765538765915876,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07210508732008293,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01862242414995047,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.075935447114891,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9827848205079635,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8646738364914638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3561605804914256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.36667672602421003,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09090529242379566,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2767161229527778,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07239486716388602,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018259577075137638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.1107675699163759,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.05521826573075363,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01861944580981255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.025181810930029157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007641127368583021,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.001963260745462347,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005759277038702137,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015206062961056117,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003980510481680037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020172575966507244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.01727839986102796,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010593850743496482,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012673147684872373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005847429833531276,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0018486889955660849,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004661110527985009,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.001418238881506766,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00038537137916531006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000008732162125872381,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010098140901345371,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010341681143796217,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009925263261987635,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009949308282635001,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009697906723789856,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000938866590730051,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009918199700435044,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009067270947776164,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000011245303139114188,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009492792535681972,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009496238369585455,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010470418436995526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009022552728697094,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010271925220030053,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009694121907992645,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009684603454500868,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009838643726852283,
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
          "id": "b38d827107303c4640808c16b8ec6caffa5e3099",
          "message": "Merge pull request #264 from telicent-oss/release/0.35.0\n\nComplete Release 0.35.0",
          "timestamp": "2026-02-04T12:32:53Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/b38d827107303c4640808c16b8ec6caffa5e3099"
        },
        "date": 1770626733273,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.185553662338871,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 3.035825741813809,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8923605512343553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.38917566092393,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3707763467646332,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09389863882643742,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.28050192262496887,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07388628571558804,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01857028746717888,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.062253978913131,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 3.0190049815211157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8878856457088625,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.353064424386322,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.36718031718663807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09371623615203314,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2793995315494291,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0741915548166103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018362970820306735,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.1119227496459925,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.05565029173407514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0186719867904922,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.02535304595462773,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007591025119400435,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019917991353441515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.00580005563503471,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015293710697705438,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00040781940081797703,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.0202085076316526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.01707874259202145,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010734854800771163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012698505856851575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005850355655241385,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0018006963621186724,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004650404435207613,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0014366935792008773,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003946285727294225,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010828183109001373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010238648248212683,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010476467578366612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010135313210283768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010583875671265139,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000011325178401105662,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010478067009317762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001114653679366229,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009226000574650971,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009665401025593565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010029643810379893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009221056683934668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010969028970000214,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000011451673614899636,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010869898780154494,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009522310861492973,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010142310710068994,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010148892464613166,
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
          "id": "8bc90e92b5bbaee3b926d07b7941d63810eb237c",
          "message": "Merge pull request #265 from telicent-oss/dependabot/maven/patches-b71ceb3b82\n\nBump the patches group with 7 updates",
          "timestamp": "2026-02-10T08:01:54Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/8bc90e92b5bbaee3b926d07b7941d63810eb237c"
        },
        "date": 1771231435476,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.160767318001047,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.9805391575616444,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8778175955272074,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.349826263232159,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.36175560420593306,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09220481616153539,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.27160809653337253,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07160532690973462,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017800392032950835,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.049249743184074,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.8918528152496483,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8687121864104318,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3321344687581533,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.36064440634750256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09156751734059262,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2752181049024841,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07172483867976011,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017810774941748958,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.11161103441512994,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.055302130414836406,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018605405547122235,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.026107257698010332,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.0075715202668841615,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019523578889219587,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.0057427612662911765,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015259087127603133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00039959550390806007,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020100870263954847,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.01708086975834797,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.010651830150887949,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.012669130080843685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005945038304987089,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.001790568464008645,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004657353732426995,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0014381667657448315,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003929784937109464,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010418618137108528,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010133051860788785,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009004584297120723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009379054724415501,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010257481830511132,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009997505077601651,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010243167050750494,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010183257836123197,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009293863981729935,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009511396360502385,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009440715204505768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009173787137354047,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000975143263298037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.00001034111835694567,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010520157970139607,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.0000108205178690668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009882913925197355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009432538982044445,
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
          "id": "d10667e90b957397f727ce27dc34fc1fe901cb26",
          "message": "Merge pull request #267 from telicent-oss/dependabot/maven/patches-8785ebac60\n\nBump ch.qos.logback:logback-classic from 1.5.28 to 1.5.32 in the patches group",
          "timestamp": "2026-02-23T08:27:20Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/d10667e90b957397f727ce27dc34fc1fe901cb26"
        },
        "date": 1771836278565,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.015554120926026,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 3.3325155507754785,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 1.0170702889383567,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3764913130246392,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.41471303229072154,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.10063350652310546,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.30990641943560104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.08039955578863861,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.02128435747916851,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.038355056082116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 3.4482544984223047,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.9089252851534475,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.5268913839649643,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.4206197682510454,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09982041144517519,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.3167414002233548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07872431069909282,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.021003835523279047,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.10758193918153769,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.053347053973351745,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.017812628691443077,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.026217395600778452,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007322273701299774,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0018886574050853572,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.005463134649257259,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.001466926370461015,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003818871606699458,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.018554502385699518,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.015461124345071534,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.009770605370731681,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.011651431793053208,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005519163113194534,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0016921246708623678,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004358871861315295,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0013695899478557091,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00037564016821108775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010080741540920187,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000008738759532537133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009025588166408412,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.00001007763636851827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000008800424733805532,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009977709441965596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.0000099450993603661,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000008581107523990816,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000008729817129498116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000008478285963696508,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.00000814275004312027,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009521487964195696,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000008925830236535604,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009416046625011898,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009145728438380275,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000007731478014035487,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009316300232628321,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.00000931126813900969,
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
          "id": "bc157621a11d494585c60193ee720c6e4acfef64",
          "message": "Merge pull request #273 from telicent-oss/dependabot/docker/docker/telicent/telicent-java21-1.2.41\n\nBump telicent/telicent-java21 from 1.2.40 to 1.2.41 in /docker",
          "timestamp": "2026-02-27T08:51:00Z",
          "url": "https://github.com/telicent-oss/smart-caches-core/commit/bc157621a11d494585c60193ee720c6e4acfef64"
        },
        "date": 1772440995778,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.14072979325856,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 3.001680837203256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.889646223937274,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3794248579951276,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3717920275607742,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09451648172269843,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.27986673957216107,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07430741417769894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.01839848476759228,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 6.154763851056218,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 2.756191837560199,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.8871899109610724,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 1.3626440868406426,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.3720899331549924,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.09483952363057363,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.2779544930279587,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.07371496592728488,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.decodeWithoutVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018537628541251225,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.11268321513931343,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.055554581402796746,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.018610101552308418,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.0256598997863929,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.007594821800630885,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0019824203033460396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.0057895944036534825,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.0015199639750188723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0004009884848699215,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.020503947844820233,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.01732664210890519,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0107180308375935,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.01263829842503211,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.005811946837179539,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0018193965374251806,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.004621076538935895,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.001418492917413666,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseAndVerify ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.0003938984477773768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.00001023029540996566,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000009547255558802063,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009786499207719927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000010352076719490273,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010345365526209385,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000009596534117210254,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009844634052926529,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010190204700717879,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"HS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010363765351108118,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009474400636992975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"512\"} )",
            "value": 0.000011280737847001315,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"1\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010621208456877833,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"128\"} )",
            "value": 0.000009697961887048087,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"512\"} )",
            "value": 0.000010092617630732237,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"10\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010420594608511344,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"128\"} )",
            "value": 0.00000985551803632044,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"512\"} )",
            "value": 0.00000947812013861595,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.telicent.smart.caches.benchmarks.JwtParsingBenchmark.parseWithRotatingKeys ( {\"algorithm\":\"RS256\",\"claimCount\":\"50\",\"claimValueLength\":\"2048\"} )",
            "value": 0.000010590197619709492,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}