#!/bin/bash
# Generate test traces

if [ $(id -u) -ne 0 ]; then
    echo "This program must be run as root"
    exit 1
fi

echo "ltt-armall"
/usr/bin/ltt-armall -q -n

# FIXME: return code of ltt-armall is broken
#RET=$?
#if [ $RET -ne 0 ]; then
#    echo "error with ltt-armall, abording"
#    exit 1
#fi

echo "starting..."

ABSPATH=$(readlink -f $0)
BASEDIR=$(dirname $ABSPATH)

export TRACE_DIR=${TRACE_DIR:-$BASEDIR/traces}
export TRACE_SCRIPTS=${TRACE_SCRIPTS:-$BASEDIR/scripts}
if [ -x "$1" ]; then
	SCRIPT_ONE=$(readlink -f $1)
fi
SCRIPT_ALL=$(find $TRACE_SCRIPTS -type f)
SCRIPT_RUN=${SCRIPT_ONE:-$SCRIPT_ALL}

mkdir -p $TRACE_DIR

for SCRIPT in $SCRIPT_RUN; do
    NAME=$(basename $SCRIPT)
    echo "tracing" $NAME
    TRACE_PATH=$TRACE_DIR/$NAME
    rm -rf $TRACE_PATH
    lttctl -o channel.all.bufnum=8 -C -w $TRACE_PATH $NAME 
    sleep 1
    $SCRIPT
    sleep 1
    lttctl -D $NAME
done

