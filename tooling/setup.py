import re
import sys
from pyhocon import ConfigFactory, HOCONConverter
import os
import boto3
from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
from Crypto.Util.Padding import pad
import base64

def write_config_secrets(config_secrets):

    config = ConfigFactory.parse_file(f'{os.environ["WHIM_HOME"]}/config/application.conf')
    kms_key_arn = config['crypto.kms.kek-arn']
    kms_encryption_algorithm = config['crypto.kms.encryption-algorithm']
    dek_size = config['crypto']['dek-size']

    dek = get_random_bytes(dek_size // 8)

    kms_client = boto3.client('kms')
    encrypted_dek = kms_client.encrypt(
        KeyId=kms_key_arn,
        Plaintext=get_random_bytes(dek_size // 8),
        EncryptionAlgorithm=kms_encryption_algorithm
    )['CiphertextBlob']

    for full_key, value in config_secrets:
        iv = get_random_bytes(AES.block_size)
        cipher = AES.new(dek, AES.MODE_CBC, iv)
        ciphertext = cipher.encrypt(pad(value.encode('utf-8'), AES.block_size))
        key_components = full_key.split('.')
        key = config
        for component in key_components[:-1]:
            key = key[component]
        key[key_components[-1]] = base64.b64encode(encrypted_dek + iv + ciphertext).decode('utf-8')

    with open(f'{os.environ["WHIM_HOME"]}/config/application.conf', 'w') as config_file:
        config_file.write(HOCONConverter.convert(config, "hocon"))


if __name__ == '__main__':

    if os.environ['WHIM_HOME'] is None:
        print("Set $WHIM_HOME before running this script. exiting.")

    args = set(map(lambda arg: arg.lower(), sys.argv[1:]))
    argument_string = ' '.join(sys.argv[1:])

    help = '-h' in args or '--help' in args

    print(
f"""
Hi there! Welcome to the whim setup tool.{" Use the option --help (-h) to get more info." if not help else ""}
"""
    )

    if help:
        print(
"""
Available Commands:
  secret - encrypt a key-value config secret using the generated dek and place it in the application.conf file. NB do not use quotes here; sometimes api keys / secrets will contain quotes, and we want to encode this properly. 
"""
        )

    if 'secret' in argument_string:
        secret_pattern = r"secret\s+([\w\-\.]+)=(.+?)(?:\s|$)"
        matches = re.findall(secret_pattern, argument_string)
        write_config_secrets([match for match in matches])
        