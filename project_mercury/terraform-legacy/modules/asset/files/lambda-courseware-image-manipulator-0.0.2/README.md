# aero-service-lambda-image-manipulator
An AWS Lambda to resize images.

The lambda should be subscribed to an AWS SNS topic and launched as needed.

Each operation/SNS record will:

1. Download a file from S3
2. Resize the image to be within given threshold while maintaining aspect ratio
3. Upload the result to S3
4. return SNS message with updated URL


## Deployment
```
npm run package
```

There's a common issue with Sharp and lambda using linux-x64 binaries, [this fix](https://github.com/lovell/sharp/issues/1459) is applied in the package script, you may need to remove 

## Input
Set MessageAttribute of `bucketName`

Then include message body consisting of minimum:

```json
{
  "url": string,
  "size": string,
  "threshold": number,
  "originalWidth": number,
  "originalHeight": number
}
```

### Output

Returns the input with updated URL to the new image or an error.

Relevant SNS queues [listed here]('./app/src/core/services/aws/sns/types.ts')

## Example Record (Full)


```json
{
				"Type": "Notification",
				"MessageId": "fa5c2b48-f361-57df-9d98-73182d005008",
				"TopicArn": "arn:aws:sns:ap-southeast-2:716062133555:timprice-courseware-asset-resize-submit",
				"Subject": null,
				"Message": "{\"notificationId\":\"97d189d0-3dbc-11eb-9dbc-856dceb2c2f3\",\"url\":\"974a7f80-3dbc-11eb-9dbc-856dceb2c2f3/original/f7cbd60764def320064f85b15920a20f.jpg\",\"assetId\":\"974a7f80-3dbc-11eb-9dbc-856dceb2c2f3\",\"originalWidth\":333.0,\"originalHeight\":500.0,\"threshold\":300.0,\"size\":\"small\"}",
				"Timestamp": "2020-12-14T03:29:37.513Z",
				"SignatureVersion": "1",
				"Signature": "DVVMUQJcp/HVRQAP7qUGuZBkwSaMIhmUFLX2CpDeVPpMc6Rwe5uvh9qP0UK8Lk+DcgTC1xLTHMTCG+nb+42yt3ptv9BtFjOiLkfxRLETP5V7uGoUrIBrWe5CXfhGEKFqZi8w9byNOislgxuq/4g2fBo+1gFcBx4p2bOZc4aKP1PQwsIhJQNPJfd0DmNm84iw8daX+bt3lNkOfvtD1rvBatYs1eeDXXgQFB0rwlnslENLiVH6q47Zj25+/R/5paZP/LQ1+PD0+frdkeir6MVU/qSsZhLdhLq8QA8tFCVUQzIRAtDc+pwEAwqFUnwCwnddRBVwkRTwBVHd/HiGy/2mOQ==",
				"SigningCertUrl": "https://sns.ap-southeast-2.amazonaws.com/SimpleNotificationService-010a507c1833636cd94bdb98bd93083a.pem",
				"UnsubscribeUrl": "https://sns.ap-southeast-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:ap-southeast-2:716062133555:timprice-courseware-asset-resize-submit:4328b70d-5ffc-4d29-b5ba-9e21b81fec98",
				"MessageAttributes": {
					"bucketName": {
						"Type": "String",
						"Value": "local-dev-aero.ap-southeast-2.bronte.dev-prsn.com"
					}
				}
			}

```

