#!/bin/bash

# run the different tests

# turn echo on and error on earliest command
set -ex

# get remote exec variables
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"
source $SCRIPT_DIR/defaults.sh

DISABLE_SIM_PREREQ="BREAK_SIM_PREREQ=1"

run_bmark () {
    make run-bmark-tests-fast -j$CI_MAKE_NPROC -C $LOCAL_SIM_DIR $DISABLE_SIM_PREREQ ${mapping[$1]} $@
}

run_asm () {
    make run-asm-tests-fast -j$CI_MAKE_NPROC -C $LOCAL_SIM_DIR $DISABLE_SIM_PREREQ ${mapping[$1]} $@
}

run_both () {
    run_bmark $@
    run_asm $@
}

run_tracegen () {
    make tracegen -C $LOCAL_SIM_DIR $DISABLE_SIM_PREREQ ${mapping[$1]} $@
}

run_binary () {
    make run-binary-fast -C $LOCAL_SIM_DIR $DISABLE_SIM_PREREQ ${mapping[$1]} $@

case $1 in
    chipyard-rocket)
        run_bmark
        make -C $LOCAL_CHIPYARD_DIR/tests
        # Test run-binary with and without loadmem
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/hello.riscv LOADMEM=1
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/hello.riscv
        ;;
    chipyard-dmirocket)
        # Test checkpoint-restore
        $LOCAL_CHIPYARD_DIR/scripts/generate-ckpt.sh -b $RISCV/riscv64-unknown-elf/share/riscv-tests/benchmarks/dhrystone.riscv -i 10000
        run_binary LOADARCH=$PWD/dhrystone.riscv.0x80000000.10000.loadarch
        ;;
    chipyard-boom)
        run_bmark
        ;;
    chipyard-dmiboom)
        # Test checkpoint-restore
        $LOCAL_CHIPYARD_DIR/scripts/generate-ckpt.sh -b $RISCV/riscv64-unknown-elf/share/riscv-tests/benchmarks/dhrystone.riscv -i 10000
        run_binary LOADARCH=$PWD/dhrystone.riscv.0x80000000.10000.loadarch
        ;;
    chipyard-spike)
        run_bmark
        ;;
    chipyard-hetero)
        run_bmark
        ;;
    rocketchip)
        run_bmark
        ;;
    chipyard-hwacha)
        make run-rv64uv-p-asm-tests -j$CI_MAKE_NPROC -C $LOCAL_SIM_DIR $DISABLE_SIM_PREREQ ${mapping[$1]}
        ;;
    chipyard-gemmini)
        GEMMINI_SOFTWARE_DIR=$LOCAL_SIM_DIR/../../generators/gemmini/software/gemmini-rocc-tests
        rm -rf $GEMMINI_SOFTWARE_DIR/riscv-tests
        cd $LOCAL_SIM_DIR
        run_binary BINARY=$GEMMINI_SOFTWARE_DIR/build/bareMetalC/aligned-baremetal
        run_binary BINARY=$GEMMINI_SOFTWARE_DIR/build/bareMetalC/raw_hazard-baremetal
        run_binary BINARY=$GEMMINI_SOFTWARE_DIR/build/bareMetalC/mvin_mvout-baremetal
        ;;
    chipyard-sha3)
        (cd $LOCAL_CHIPYARD_DIR/generators/sha3/software && ./build.sh)
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/generators/sha3/software/tests/bare/sha3-rocc.riscv
        ;;
    chipyard-mempress)
        (cd $LOCAL_CHIPYARD_DIR/generators/mempress/software/src && make)
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/generators/mempress/software/src/mempress-rocc.riscv
        ;;
    chipyard-manymmioaccels)
	make -C $LOCAL_CHIPYARD_DIR/tests

	# test streaming-passthrough
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/streaming-passthrough.riscv

	# test streaming-fir
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/streaming-fir.riscv

	# test nvdla
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/nvdla.riscv

	# test fft
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/fft.riscv
	;;
    chipyard-manyperipherals)
	# SPI Flash read tests
        make -C $LOCAL_CHIPYARD_DIR/tests
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/spiflashread.riscv
        ;;
    chipyard-spiflashwrite)
        make -C $LOCAL_CHIPYARD_DIR/tests
        run_binary BINARY=$LOCAL_CHIPYARD_DIR/tests/spiflashwrite.riscv
        [[ "`xxd $LOCAL_CHIPYARD_DIR/tests/spiflash.img  | grep 1337\ 00ff\ aa55\ face | wc -l`" == "6" ]] || false
        ;;
    tracegen)
        run_tracegen
        ;;
    tracegen-boom)
        run_tracegen
        ;;
    chipyard-cva6)
        run_binary BINARY=$RISCV/riscv64-unknown-elf/share/riscv-tests/benchmarks/multiply.riscv
        ;;
    chipyard-ibex)
        # Ibex cannot run the riscv-tests binaries for some reason
        # run_binary BINARY=$RISCV/riscv64-unknown-elf/share/riscv-tests/isa/rv32ui-p-simple
        ;;
    chipyard-sodor)
        run_asm
        ;;
    chipyard-constellation)
        run_binary LOADMEM=1 BINARY=$RISCV/riscv64-unknown-elf/share/riscv-tests/benchmarks/dhrystone.riscv
        ;;
    icenet)
        run_binary BINARY=none
        ;;
    testchipip)
        run_binary BINARY=none
        ;;
    constellation)
        run_binary BINARY=none
        ;;
    *)
        echo "No set of tests for $1. Did you spell it right?"
        exit 1
        ;;
esac
