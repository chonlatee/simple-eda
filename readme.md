# simple-eda

#### just simple eda and mda for insurance claim fraud detection.

step:
- docker-compose up -d
- open browser http://localhost:8080
    - create topic
        - insurance-claims
        - insurance-home-claim-fraud
        - small-claim-amount-topic
        - large-claim-amount-topic
- open browser http://localhost:15672
    - login with guest/guest
    - create queue
        - q.claim-email
- open claim-detector 
    - ./mvnw spring-boot:run
- open fraud-home-monitoring
    - npm install or yarn install
    - node server.js  
    - open http://localhost:3333 in browser
- open dashboard
    - npm install or yarn install
    - node server.js  
    - open http://localhost:3000 in browser
- open emailsvc
    - go run main.go


- call url GET http://localhost:8888/send-batch-claim to send batch claim data to kafka topic `insurance-claims`
- call url GET http://localhost:3000/delete-claims for delete all claims in database dashboard

