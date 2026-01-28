#!/bin/bash

echo "🚀 Starting AWS Resource Cleanup..."
echo "This script will delete resources created by AWS_DEPLOY_GUIDE_V2.md"
echo "Region: ${REGION:-ap-northeast-2}"

# 1. EC2 Instances (Terminate)
echo "🔍 Searching for EC2 instances..."
INSTANCE_IDS=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=Entrusting-Ubuntu,Trustee-*,vpass-*" "Name=instance-state-name,Values=running,stopped" \
  --query "Reservations[].Instances[].InstanceId" --output text)

if [ -n "$INSTANCE_IDS" ]; then
    echo "⚠️ Terminating Instances: $INSTANCE_IDS"
    aws ec2 terminate-instances --instance-ids $INSTANCE_IDS
    echo "⏳ Waiting for instances to terminate (this may take a minute)..."
    aws ec2 wait instance-terminated --instance-ids $INSTANCE_IDS
    echo "✅ Instances terminated."
else
    echo "✅ No active instances found."
fi

# 2. RDS Database (Delete)
echo "🔍 Checking RDS instance 'entrusting-db-v2'..."
RDS_STATUS=$(aws rds describe-db-instances --db-instance-identifier entrusting-db-v2 --query "DBInstances[0].DBInstanceStatus" --output text 2>/dev/null)

if [ "$RDS_STATUS" == "available" ] || [ "$RDS_STATUS" == "stopped" ]; then
    echo "⚠️ Deleting RDS instance 'entrusting-db-v2' (Skip Final Snapshot)..."
    aws rds delete-db-instance --db-instance-identifier entrusting-db-v2 --skip-final-snapshot
    echo "⏳ RDS deletion initiated. It will be deleted in the background."
elif [ -z "$RDS_STATUS" ]; then
    echo "✅ RDS instance not found."
else
    echo "ℹ️ RDS status is '$RDS_STATUS'. Skipping deletion command."
fi

# 3. Security Groups (Delete)
# Note: Can only be deleted after instances are terminated.
echo "🔍 Deleting Security Group 'entrusting-web-sg-v2'..."
SG_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=entrusting-web-sg-v2" --query "SecurityGroups[0].GroupId" --output text 2>/dev/null)

if [ -n "$SG_ID" ] && [ "$SG_ID" != "None" ]; then
    echo "⚠️ Deleting SG: $SG_ID"
    # Retry loop because it takes time for terminated instances to release SG
    for i in {1..5}; do
        aws ec2 delete-security-group --group-id $SG_ID && break || sleep 10
    done
else
    echo "✅ Security Group not found."
fi

# 4. Key Pair (Delete)
echo "🔍 Deleting Key Pair 'entrusting-key-v2'..."
aws ec2 delete-key-pair --key-name entrusting-key-v2
if [ -f "entrusting-key-v2.pem" ]; then
    rm entrusting-key-v2.pem
    echo "🗑️ Deleted local key file 'entrusting-key-v2.pem'"
fi
echo "✅ Key Pair deleted."

echo "🎉 Cleanup Complete!"
