.PHONY: run package run-jar docker-down docker-logs

run:
	docker compose up --build

package:
	mvn -DskipTests package

run-jar: package
	java -jar target/event-pooling-challenge-1.0.0-SNAPSHOT.jar

docker-down:
	docker compose down

docker-logs:
	docker compose logs -f
