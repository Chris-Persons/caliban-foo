# caliban-foo


curl -X POST    http://localhost:8088/api/graphql    -H 'Host: localhost:8088'    -H 'Content-Type: application/json'    -d '{"query": "query { numbers}"}' | json_pp

curl -X POST    http://localhost:8088/api/graphql    -H 'Host: localhost:8088'    -H 'Content-Type: application/json'    -d '{"query": "query { head }"}' | json_pp

curl -X POST    http://localhost:8088/api/graphql    -H 'Host: localhost:8088'    -H 'Content-Type: application/json'    -d '{"query": "mutation { pop }"}' | json_pp

curl -X POST    http://localhost:8088/api/graphql    -H 'Host: localhost:8088'    -H 'Content-Type: application/json'    -d '{"query": "mutation { push(num: 6) }"}' | json_pp
