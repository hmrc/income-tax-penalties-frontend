
# income-tax-penalties-frontend

Frontend for MTD ITSA Penalty Reform

## Testing

Start supporting services:

```
  sm2 -start MONGO ASSETS_FRONTEND_2 AUTH AUTH_LOGIN_API AUTH_LOGIN_STUB IDENTITY_VERIFICATION USER_DETAILS
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

Run income-tax-penalties-frontend (this repo)

```  
  cd income-tax-penalties-frontend
  sbt run
```

In a web browser:
-  navigate to http://localhost:9185/income-tax-penalties-frontend
-  Enter enrolment key HMRC-MTD-IT ~ MTDITID ~ 12345
-  Enter NINO that matches a test case in the stubs

### Test NINOs

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