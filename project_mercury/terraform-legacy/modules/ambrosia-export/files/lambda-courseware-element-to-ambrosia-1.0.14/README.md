# Overview

This project is intended to be executed as an AWS lambda function which consumes SNS events.
It handles the conversion of a courseware element into an ambrosia snippet.

## Package for AWS Lambda

Perform the following steps in order to create an AWS Lambda .zip file (`lambda-courseware-element-to-ambrosia-x.y.z.zip`)

```bash
npm install
npm pack
```
