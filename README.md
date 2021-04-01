
# integration-catalogue-api-platform-tools

This is a placeholder README.md for a new repository

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


# Platform API OAS extract tool

For extracting API's in OpenAPI Specification for publishing into the API Catalogue.

# To run and import into the Integration Catalog

```
sbt run
```

This will output all the QA API Platform OAS3.0 folder here `generated\`

Replace the existing files in the integration catalogue with these and update the files list if it has changed (`ListOfFiles.listOfApiPlatformFiles`).

It is important to check for any errors as well as count how many files you expect to be generated. Sometimes when imported into the catalogue some files are invalid, and need manually correcting. 
These files typically need an example fixing in the OAS (or removing):

- `self-assessment-api-2.0.yaml`
- `vat-api-1.0.yaml`
# Notes

This uses this library for parsing RAML and converting to OAS 3: [webapi-parser](https://raml-org.github.io/webapi-parser/migration-guide-java.html).

# Steps

1. Run 'api-definition-csv-export.js' on Mongo Jenkins job in an environment (e.g. `External Test`)
1. Copy output over 'apis-to-import'
1. Diff and re-add any raml override paths in the CSV.
1. Generate the git clone command
    ```
    sbt 'run --generateGitClone' > api-repos/clone-apis.sh
    ```
1. Tidy up the 'clone-apis.sh' bash script. e.g. delete all the sbt log messages at the top, and add this as the fist line.
    ```
    #!/usr/bin/env bash
    ```
    
1. Run the 'clone-apis.sh' from the 'api-repos' directory (this will take a few minutes to run!)
1. Generate the OAS files with this script `export_oas.sh`.
1. Check for any errors in the OAS generate (mostly where it can't find the `application.raml` file).
1. Copy and replace the OAS files in `integration-catalogue-oas-files` and import into the catalogue (checking for any publishing errors)
# Notes on imports
## 2021-04-1
### Problem APIs (that have not been imported)
- `import-control-entry-declaration-intervention`     
  - IllegalTypeDeclarations
  - `import-control-entry-declaration-intervention;Safety and Security Import Notifications;1.0;PUBLIC;public/api/conf`
- `import-control-entry-declaration-outcome`
  - IllegalTypeDeclarations
  - `import-control-entry-declaration-outcome;Safety and Security Import Outcomes;1.0;PUBLIC;public/api/conf`
- `import-control-entry-declaration-store`
  - IllegalTypeDeclarations
  - `import-control-entry-declaration-store;Safety and Security Import Declarations;1.0;PUBLIC;public/api/conf`
- `native-apps-api-orchestration`
  - Doesn't seem to be a real MS on MDTP.
- `personal-income`
  - Doesn't seem to be a real MS on MDTP.
- `sso`
   - Doesn't seem to be a real API Platform API (doesn't have definition endpoints)
- `sso-frontend`
   - Doesn't seem to be a real API Platform API (doesn't have definition endpoints)
- `self-assessment-api-2.0.yaml`
   - Doesn't publish. OAS errors
      ```
      Error: Failed to publish 'self-assessment-api-2.0.yaml'. Response(400): {"errors":[{"message":"sequence entries are not allowed here\n in 'reader', line 11155, column 20:\n              example: -\n                       ^\n"}]}
      ```
- `vat-api-1.0.yaml`
   - Doesn't publish. OAS errors
      ```
      Failed to publish 'vat-api-1.0.yaml'. Response(400): {"errors":[{"message":"sequence entries are not allowed here\n in 'reader', line 1206, column 20:\n              example: -\n                       ^\n"}]}
      ```
