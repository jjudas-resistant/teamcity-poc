import boto3
from moto import mock_s3

from bp_project.something import whatever


@mock_s3
def test_whatever() -> None:
    client = boto3.client('s3')
    client.create_bucket(Bucket='bucket', CreateBucketConfiguration={'LocationConstraint': 'eu-west-1'})
    client.put_object(Bucket='bucket', Key='file.txt', Body='abcd')

    result = whatever(client, 'bucket', 'file.txt')

    assert result == 'abcd'


class TestMore:
    def test_something(self) -> None:
        assert 1 < 6
