
# income-tax-penalties-frontend

Frontend for MTD ITSA Penalty Reform

## Testing

Start supporting services:

```
  sm2 -start MONGO ASSETS_FRONTEND_2 AUTH AUTH_LOGIN_API AUTH_LOGIN_STUB IDENTITY_VERIFICATION USER_DETAILS SI_PROTECTED_USER_LIST_ADMIN
```

Run penalties-backend (in a separate shell):

```  
  git clone git@github.com:hmrc/penalties-backend.git
  cd penalties-backend
  sbt run
```

Run income-tax-penalties-stubs (in a separate shell):

```  
  git clone git@github.com:hmrc/income-tax-penalties-stubs.git
  cd income-tax-penalties-stubs
  sbt run
```

Run income-tax-penalties-frontend (this repo):

```  
  cd income-tax-penalties-frontend
  sbt run
```

Alternatively, start all supporting services including the penalties microservices by running:

```
  sm2 --start INCOME_TAX_PENALTIES_ALL
```

In a web browser:
-  navigate to http://localhost:9185/penalties/income-tax
-  Enter enrolment key HMRC-MTD-IT ~ MTDITID ~ 12345
-  Enter NINO that matches a test case in the stubs

### Test NINOs for LSP individuals

| NINO      | Scenario                                                                                     |
| --------- |----------------------------------------------------------------------------------------------|
| AB123456D | User with no penalties                                                                       |
| TT217906A | One LSP point                                                                                |
| GP789349C | One LSP point                                                                                |
| GP406035B | One LSP point                                                                                |
| WC985350D | One LSP point                                                                                |
| LM325099C | Two LSP points                                                                               |
| RE406480A | Three LSP points, including LSP for late annual submission                                   |
| JM245815B | Four LSP points, including LSP for late annual submission and £200 penalty                   |
| GH071208D | Five LSP points, including all four penalty points from above and an additional £200 penalty |

### Test NINOs for LPP individuals

| NINO       | Scenario                            |
|------------|-------------------------------------|
| OC262468C  | One estimated LPP                   |
| RB277251A  | One estimated LPP and one paid LPP  |
| MP687843A  | Second estimated LPP                |
| WC031371C  | Second increased estimated LPP      |
| ZM952872A  | Second LPP due                      |
| WG809536D  | All LPPs paid                       |

To test agent scenarios, in a web browser:
-  Navigate to http://localhost:9949/auth-login-stub/gg-sign-in?continue=http://localhost:9185/income-tax-penalties/test-only/set-delegation
-  Select agent affinity
-  Select 200 for the Confident Level
-  Enter HMRC-MTD-IT for delegated enrolment key, MTDITID for Identifier Name, a NINO that matches a test case in the stubs for Identifier Value and mtd-it-auth for Delegated Auth Rule 
-  Click submit
-  Enter the same MTDITID you entered before
-  Enter the same NINO you entered before

### Test NINOs for LSP agents

| NINO      | Scenario                                                                                     |
| --------- |----------------------------------------------------------------------------------------------|
| YE954947B | One LSP point                                                                                |
| AB308469A | Two LSP points                                                                               |
| AB246346D | Three LSP points, including LSP for late annual submission                                   |
| AB889378B | Four LSP points, including LSP for late annual submission and £200 penalty                   |
| KS294480A | Five LSP points, including all four penalty points from above and an additional £200 penalty |

### Test NINOs for LPP agents

| NINO       | Scenario              |
|------------|-----------------------|
| KA983666C  | One estimated LPP     |
| RB124256B  | Second estimated LPP  |     

## Test-only enpoints

To start the micro-service locally using the test routes run the following command: 

``` sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes ```

When testing manually and needing to initialise the identifiers for a session, visit:

```GET        /income-tax-penalties/test-only/set-delegation```

Automated tests can post form-urlencoded identifiers "mtditd" and "nino" to the following endpoint: 

```POST       /income-tax-penalties/test-only/set-delegation```

To switch the session service feature put a json true or false to:

```PUT        /income-tax-penalties/test-only/feature/use-session-service```

To switch the auth optimisation feature put a json true or false to:

```PUT        /income-tax-penalties/test-only/feature/optimise-auth-for-individuals```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
