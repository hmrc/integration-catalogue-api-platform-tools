
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
