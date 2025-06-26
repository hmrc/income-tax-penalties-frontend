
# Income Tax Penalties Frontend

This is Scala/Play service that allows Income Tax users/agents to view their/their clients penalties and appeals.

It also allows users to appeal the penalties, view how the penalties were calculated and to see when their penalties will be removed from their account.

This service does not use Mongo.

## Running
This application runs on port 9185.

You can use the `./run.sh` to run the service.

The user must have an authenticated session and be enrolled in MTD Income Tax Self Assessment users to access most pages of this service.

The service manager configuration name for this service is: `INCOME_TAX_PENALTIES_FRONTEND`

This service is dependent on other services, all dependent services can be started with
`sm2 --start INCOME_TAX_PENALTIES_ALL`.

## Testing

This service can be tested with SBT via `sbt test` and `sbt it/test`

To run coverage please run: `sbt clean coverage test it/test coverageReport`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
