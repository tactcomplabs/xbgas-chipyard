package chipyard

import org.chipsalliance.cde.config.{Config}

class AxinsimpleQuadRocketConfig extends Config(
  new axinsimple.WithAxinsimple(base=0x223000L, size=0x1000L) ++
  new freechips.rocketchip.subsystem.WithNBigCores(4) ++
  new chipyard.config.AbstractConfig)
