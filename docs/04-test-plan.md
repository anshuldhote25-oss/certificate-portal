# Task 9 - Selenium test plan

## Scope
Five critical user journeys of the Certificate Generation Portal, tested through Chrome with Selenium WebDriver. Tests live in `src/test/java/com/certportal/it/*IT.java` and run with `mvn verify` after the WAR is packaged.

## Approach
* The packaged WAR is started in an embedded Tomcat on a free port with an in-memory database, so the tests need no installed server and do not touch real data. Pass `-DbaseUrl=...` to test an already deployed server instead (the Jenkins pipeline can do this after deploying).
* Elements are located by `data-testid` attributes, so cosmetic changes to the page do not break the tests.
* Each test creates its own uniquely named records, so tests are independent and can run in any order.
* On failure the test saves a PNG screenshot and the page HTML in `target/screenshots` (see `ScreenshotOnFailure`).

## Journeys and test cases
| Journey | Test class | Test case | Test data | Key assertions |
|---------|-----------|-----------|-----------|----------------|
| 1 Sign in / out | `LoginIT` | validLoginShowsDashboardForTheUser | operator / operator123 | URL ends `/dashboard`; user name and role shown |
| | | wrongPasswordShowsAnErrorAndStaysOnLogin | operator / wrong | error text shown; URL stays `/login` |
| | | protectedPageRedirectsAnonymousVisitorsToLogin | no session | redirected to `/login` |
| | | logoutEndsTheSession | admin | after logout `/dashboard` redirects to login |
| 2 Create / view / update | `CertificateCrudIT` | createdCertificateIsShownAsDraftWithANumber | unique name, DevOps Essentials | flash message, name, course, status Draft, number starts `CERT-` |
| | | invalidFormShowsValidationMessagesAndSavesNothing | bad email, empty fields | four field errors shown; form redisplayed |
| | | editingADraftUpdatesTheRecord | course changed to Advanced Git | flash "updated"; new course shown; still Draft |
| 3 Search | `SearchIT` | searchFindsARecordByPartialLowercaseName | unique name typed in lower case | exactly one row with the correct name |
| | | searchWithNoMatchShowsTheEmptyState | random text | empty-state message; "0 record(s)" |
| | | statusFilterShowsOnlyRecordsWithThatStatus | status = Issued | every row shows Issued |
| 4 Workflow | `WorkflowIT` | certificateTravelsFromDraftToIssuedAcrossThreeRoles | operator, approver, admin | Draft -> Submitted -> Approved -> Issued; no actions left |
| | | rejectedCertificateCanBeEditedAndResubmitted | operator, approver | Rejected record editable, resubmit gives Submitted |
| | | operatorHasNoApproveButton | operator | approve/reject buttons absent |
| | | approverCannotCreateCertificates | approver | no "New certificate" link; direct URL gives 403 |
| 5 Dashboard | `DashboardIT` | totalEqualsTheSumOfAllStatusCounts | demo data | total equals sum of five counts |
| | | creatingACertificateIncreasesTotalAndDraftCounts | new record | total and Draft each +1 |
| | | footerShowsTheEnvironmentLabel | - | footer starts with "Environment:" |

## Failure screenshot mechanism
`mvn verify -Dit.test=FailureCaptureDemoIT -Dforce.fail=true` makes one test fail on purpose. Check `target/screenshots/*.png`. In Jenkins these files are archived with the build.

## Execution
```bash
mvn clean verify                 # unit tests, package, Selenium tests
mvn verify -Dheadless=false      # watch the browser
```
Reports: `target/surefire-reports` (unit) and `target/failsafe-reports` (Selenium, XML and text). Jenkins publishes them with the JUnit plugin.

## Exit criteria
All 17 Selenium cases (and the 20 unit tests) pass; no screenshots are produced in a green run.
