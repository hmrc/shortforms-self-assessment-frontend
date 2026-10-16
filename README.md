# shortforms-self-assessment-frontend

This is a set of 3 forms serving Self Assessment:
- SA1
- CWF1ST
- CeaseTrading

## Live examples
- https://tax.service.gov.uk/self-assessment-forms/sa1
- https://tax.service.gov.uk/self-assessment-forms/cwf1st
- https://tax.service.gov.uk/self-assessment-forms/cease-trading

## Running the app locally
This service depends on:
- Shortforms Self Assessment Backend
- Auth
- Identity Verification
- Address Lookup Frontend
- IForms Submission

To start them (or their stubs, where appropriate) locally, run:
```bash
sm2 --start DASS_SHORTFORMS_ALL
```

## Running the test suite
Run all tests and produce coverage report:
```bash
sbt clean coverage test it/test coverageReport
```

Run only unit tests:
```bash
sbt clean test
```

Run only integration tests:
```bash
sbt clean it/test
```

Run only format main sources:
```bash
sbt scalafmt
```
Run only format main and test sources code:
```bash
sbt scalafmtAll
```


## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").