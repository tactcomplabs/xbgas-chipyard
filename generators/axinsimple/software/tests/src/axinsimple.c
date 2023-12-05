#include <stdio.h>
#include "mmio.h"

#define MEMREAD32(base_, idx_) *((uint32_t*)((base_) + ((idx_) * 8)))

#define AXINSIMPLEBASE 0x223000

void test_axinsimple(void) {
    *((uint64_t*)AXINSIMPLEBASE) = 0xf00df00d;
    *((uint64_t*)(AXINSIMPLEBASE + 8)) = 0xf00df00e;

    uint64_t base_addr=AXINSIMPLEBASE;
    int how_many=2;
    for (int i=0; i < how_many; i++) {
        printf("word[%02d]: 0x%08x\n", i, MEMREAD32(base_addr, i));
    }
    return;
}

// DOC include start: GCD test
int main(void)
{
    printf("Begin test...\n");
    test_axinsimple();
    printf("Done\n");
    return 0;
}
// DOC include end: GCD test
