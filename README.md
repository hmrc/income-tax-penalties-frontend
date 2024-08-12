
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

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").