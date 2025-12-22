This document demonstrates basic AWS deployment using EC2 and Docker.

## Step 1: Build Docker Image
docker build -t shadow-ledger-service .

## Step 2: Push Image to ECR
aws ecr create-repository --repository-name shadow-ledger-service

docker tag shadow-ledger-service:latest \
<account>.dkr.ecr.<region>.amazonaws.com/shadow-ledger-service:latest

docker push <account>.dkr.ecr.<region>.amazonaws.com/shadow-ledger-service:latest

## Step 3: Create EC2 Instance

Amazon Linux 2

Open ports: 22, 8082

Install Docker

sudo yum install docker -y
sudo service docker start

## Step 4: Run Container
docker run -d -p 8082:8082 \
-e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/ledgerdb \
-e SPRING_KAFKA_BOOTSTRAP_SERVERS=<kafka-host>:9092 \
shadow-ledger-service

## Step 5: Verify
curl http://<public-ip>:8082/actuator/health

