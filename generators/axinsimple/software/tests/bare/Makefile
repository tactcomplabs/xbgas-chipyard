bmarkdir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

TARGET := riscv64-unknown-elf
CFLAGS := -mcmodel=medany -std=gnu99 -O2 -fno-common -fno-builtin-printf -Wall -fno-tree-loop-distribute-patterns
LDFLAGS := -static -nostdlib -nostartfiles -lgcc

PROGRAMS := axinsimple

objs := crt.o syscalls.o
ldscript := link.ld

include $(bmarkdir)/../common.mk
