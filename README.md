**School Management REST API** – a Spring Boot backend with secure authentication, role-based access, and token-based API protection. Built to practice real backend concerns such as security, layering, DTO mapping, error handling, and token lifecycle management.

This project was intentionally kept small in scope, but designed with real production concerns in mind. The focus was not feature count, but correctness, clarity, and decision-making.

**1. JWT-based Authentication**

The application uses JWT (JSON Web Tokens) for authentication to keep the backend stateless and horizontally scalable.

Why JWT

Avoids server-side session storage
Works naturally with REST APIs
Suitable for cloud and containerized deployments

Trade-offs considered

JWT revocation is non-trivial once issued
Token expiry is the primary safety mechanism
Design choice
Short-lived access tokens are used
Token validation is enforced through Spring Security filters
Expired or invalid tokens are handled explicitly

**In a real production system, this could be extended with refresh tokens or token blacklisting if stronger revocation guarantees are required.
**

**2. Role-based Authorization**
The system enforces role-based access control using Spring Security.

Roles

ROLE_USER
ROLE_ADMIN

Why role separation

Clear authorization boundaries
Easy to reason about permissions
Matches real enterprise access models

Authorization is enforced at the API layer, not just assumed at the UI level, ensuring backend-side security.

**3. Stateless Backend Design**

The backend is designed to be fully stateless.

What this means

No server-side session storage
Each request contains all required authentication data
Any instance can serve any request

Why this matters

Enables horizontal scaling
Works cleanly in Kubernetes environments
Simplifies failure recovery and restarts

This design mirrors how production microservices are typically deployed.

**4. Layered Architecture**

The application follows a clear layered structure:

Controller → Service → Repository


Responsibilities

Controller: Request validation, response mapping

Service: Business logic and orchestration

Repository: Data access abstraction

Why this structure

Improves readability and maintainability
Makes testing easier
Prevents tight coupling between layers
This structure avoids “fat controllers” and keeps business logic testable and isolated.

**5. Scope and Intent**
This project is intentionally not a large end-to-end system.

The goal was to:

Reinforce Spring Boot and Spring Security fundamentals
Practice secure API design
Demonstrate clean layering and defensive coding
Stay close to production-style decision making

Given real traffic and requirements, the system could be extended with:

* Refresh tokens
* Database migrations (Flyway/Liquibase)
* API versioning
* Rate limiting
* Observability and metrics

## 1. Signup Flow — POST /api/auth/signup.

**Goal: Store a new user in the database (H2 in-memory DB).**

Controller: AuthController.signup()

Client sends JSON like:

{
  "username": "kamini",
  "email": "kamini@example.com",
  "password": "pass123"
}

**Steps**:

* Controller checks if username/email already exist.

* Password is encoded using BCryptPasswordEncoder:

* user.setPassword(passwordEncoder.encode(password));

* This ensures it’s not stored as plain text.

* User is saved to H2 using UserRepository.save(user).

Result:
H2 DB now has one record:
|id	 |username|	email	|password (hashed)|	roles   |
|----|--------|-------|-----------------|---------|
|1	 |kamini 	|.......|$2a$10$8kK...	  |ROLE_USER|
## 2. Login / Token Request — POST /api/auth/token

This endpoint mimics an OAuth2 Password Grant flow.

 ### Request body:

grant_type=password&username=kamini&password=pass123

Internally:

+ Controller detects grant_type=password.

+ Calls AuthenticationManager.authenticate():

authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);


🔍 What happens here:

* AuthenticationManager delegates to the configured DaoAuthenticationProvider.

* It uses the CustomUserDetailsService to load the user from DB.

* It then checks the password using the BCryptPasswordEncoder.

* If valid, the user is authenticated successfully.

### After authentication success:

* Controller generates JWTs:

* String accessToken = jwtUtil.generateAccessToken(username, roles);

## 3. JWT Token Generation (JwtUtil)

JWT = Header + Payload + Signature

Example:

eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYW1pbmkiLCJyb2xlcyI6IlJPTEVfVVNFUiIsImlhdCI6MTcxODI1MjYxNiwiZXhwIjoxNzE4MjUzNDE2fQ.VA...

### generateAccessToken() includes:

* subject: username (kamini)

* claims: roles

* iat: issued time

* exp: expiration (15 min)

* signature: signed using HS512 with secret key

#### This ensures the token is tamper-proof — you can verify but not alter it.

## 4. Client Stores Tokens

The client gets response as token:
It saves them (usually in browser localStorage or mobile secure store).

## 5. Accessing Protected Endpoint — GET /api/test/user

The client sends:

** Authorization: Bearer <access_token>

Behind the scenes — JwtAuthFilter

* This filter runs before every request (except /api/auth/**).

Steps:

* Reads the Authorization header.

* Extracts token → validates via JwtUtil.validateToken().

* Extracts username from token.

* Loads user details from DB (CustomUserDetailsService).

* Creates a new authenticated object:

UsernamePasswordAuthenticationToken auth = 
    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);


* Puts it into the SecurityContextHolder — so Spring knows the user is logged in.

*  Once that’s set, the request continues to your controller (/api/test/user).

## 6. Controller Access

TestController is a simple endpoint:

@GetMapping("/api/test/user")
public String user() {
    return "Hello, authenticated user!";
}


* Spring Security sees the user is authenticated (from SecurityContext), so it allows the request.

* If no valid token → 403 Forbidden.

## Flow Summary
### Step	Action	Component Involved	Key Behavior
1	Signup	AuthController, UserRepository, PasswordEncoder	Save user in H2 with encoded password

2	Login	AuthController, AuthenticationManager	Verify credentials, generate tokens

3	JWT Creation	JwtUtil	Create signed access/refresh tokens

4	Protected Request	JwtAuthFilter, CustomUserDetailsService	Validate JWT, set SecurityContext

5	Controller Access	TestController	Allowed if authenticated



===============================
## CI/CD FLOW
* Push code to github
* launch EC2 instanceon AWS
* Install aws cli, docker, eksctl[kubernetes cluster], kubectl on ec2
* Clone the project on ec2 machine using git clone.
* configure aws using ``aws configure`` and ``aws sts get-caller-identity``
* create docker image using docker commands
* Make sure your user is in the Docker group:
   ``sudo usermod -aG docker $USER``
   ``newgrp docker``
    Then you can run Docker without sudo
* Push image to ECR using push commands specified in AWS ECR
* Verify the repository is created and image is pushed to ECR
* Create kube cluster using eksctl
    ``eksctl create cluster \
  --name crud-cluster \
  --region ap-south-1 \
  --nodegroup-name linux-nodes \
  --node-type t3.medium \
  --nodes 2
``
* Connect kubectl to your EKS cluster
  You must update your kubeconfig to talk to the cluster:
  `` aws eks update-kubeconfig --region ap-south-1 --name <your-cluster-name>``
* Add worker nodes , so create node port
    ``aws eks create-nodegroup \
  --cluster-name <your-cluster-name> \
  --nodegroup-name my-node-group \
  --scaling-config minSize=1,maxSize=3,desiredSize=2 \
  --disk-size 20 \
  --subnets <subnet-ids-comma-separated> \
  --instance-types t3.medium \
  --ami-type AL2_x86_64 \
  --node-role <arn-of-eks-node-role> \
  --region ap-south-1
``
* Check node group status
    ``aws eks describe-nodegroup \
  --cluster-name <your-cluster-name> \
  --nodegroup-name <your-nodegroup-name> \
  --region ap-south-1 \
  --query "nodegroup.status"
``
* Failure reason if any
    ``aws eks describe-nodegroup \
  --cluster-name <your-cluster-name> \
  --nodegroup-name <your-nodegroup-name> \
  --region ap-south-1 \
  --query "nodegroup.health"
``
* Verify ``kubectl get nodes ``
* Create deployment.yml, Service.yml
  
  #### Deployment.yml
  ``apiVersion: apps/v1
kind: Deployment
metadata:
  name: crud-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: crud-app
  template:
    metadata:
      labels:
        app: crud-app
    spec:
      containers:
      - name: crud-app
        image: <your-account-id>.dkr.ecr.ap-south-1.amazonaws.com/my-crud-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
``
#### Service.yml
``
apiVersion: apps/v1
kind: Deployment
metadata:
  name: crud-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: crud-app
  template:
    metadata:
      labels:
        app: crud-app
    spec:
      containers:
      - name: crud-app
        image: <your-account-id>.dkr.ecr.ap-south-1.amazonaws.com/my-crud-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
``
* Apply both .yaml files
* If your Deployment already exists (like your crud-app), make sure it points to the new image in ECR:
``kubectl set image deployment/crud-app \
  crud-app=811441671649.dkr.ecr.ap-south-1.amazonaws.com/security-app:latest``
### Explanation:
   * deployment/crud-app → your Deployment name
     crud-app=... → container name in the pod spec and the new image URI
    This triggers a rolling update, so old pods will terminate and new pods will pull the image from ECR.
* Expose app
  ``kubectl expose deployment crud-app --type=LoadBalancer --name=crud-app-lb --port=80 --target-port=8080``
  ``kubectl get svc crud-app-lb``
 * Use this Load Balancer url to hit services endpoints
