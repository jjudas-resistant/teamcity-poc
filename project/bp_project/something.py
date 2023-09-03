from mypy_boto3_s3 import S3Client


def whatever(client: S3Client, bucket: str, file: str) -> str:
    return client.get_object(Bucket=bucket, Key=file)['Body'].read().decode('UTF-8')
