.PHONY: incubator run-f shell build refresh-img

incubator:
	docker run --rm -it -w="/incubator" -v `pwd`:/incubator clojure bash
shell:
	docker-compose run app bash
run-f:
	docker-compose up
refresh-img: build
build:
	docker-compose build
