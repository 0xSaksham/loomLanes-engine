#!/bin/bash
echo "🚀 Launching 1000 tasks with randomized Client IDs..."
for i in {1..1000}
do
   TYPE="ORDER"
   if (( $i % 10 == 0 )); then TYPE="FAIL_ME"; fi

   # Randomizing the Client ID so the rate limiter allows more traffic
   CLIENT="user-$(( ( RANDOM % 50 )  + 1 ))"

   curl -s -X POST http://localhost:8080/api/v1/tasks \
     -H "Content-Type: application/json" \
     -H "X-Client-Id: $CLIENT" \
     -d "{\"taskId\": \"STRESS-$i\", \"type\": \"$TYPE\", \"priority\": $(( ( RANDOM % 10 )  + 1 )), \"payload\": {}}" > /dev/null &
done
wait
echo "✅ All tasks dispatched."