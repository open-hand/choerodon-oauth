# oauth-server

This service is the authorized authentication center of the choerodon microservices framework and is mainly responsible for user privilege and authorization.

## Feature

Add third-party authorized login function (WeChat login, etc.)
## Requirements

The oauth-server depends on the iam-service database, so make sure that the iam-service database is initialized before using it.
## To get the code

```
git clone https://github.com/choerodon/oauth-server.git
```
## Installation and Getting Started

* database：
The iaro-service database of the used choerodon microservices framework

* Then run the project in the root directory of the project：
```sh
mvn spring-boot:run
```

## Usage

1. User login authorization
    * The user completes the authorization in oauth through the username password.
    * Oauth will produce an access_token based on the user and the authenticated client, and save it.
1. Access Resource Service Certification for user
    * The user requests carrying the access_token. After the oauth finishes checking, the request is forwarded by the gateway to the corresponding resource service.
    * return a 401 error for user request illegally and jumps to the login page to reauthorize.
Find the information you want here
## Dependencies

* mysql
* kafka

## Reporting Issues

If you find any shortcomings or bugs, please describe them in the Issue.
    
## How to Contribute
Pull requests are welcome! Follow this link for more information on how to contribute.

