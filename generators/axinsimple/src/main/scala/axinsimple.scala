package axinsimple

import chisel3._
import chisel3.util._
import freechips.rocketchip.subsystem.{BaseSubsystem, CacheBlockBytes}
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, IdRange}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.tilelink._

case class AxinsimpleConfig(base: BigInt, size: BigInt)
case object AxinsimpleKey extends Field[Option[AxinsimpleConfig]](None)

class AxinsimpleWrapper(beatBytes: Int)(implicit p: Parameters) extends LazyModule {
  val config = p(AxinsimpleKey).get
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    Seq(AXI4SlaveParameters(
      address       = AddressSet.misaligned(config.base, 0x2000L),
      // resources     = resources,
      regionType    = RegionType.UNCACHED,
      executable    = false,
      supportsRead  = TransferSizes(1, beatBytes),
      supportsWrite = TransferSizes(1, beatBytes),
      interleavedId = Some(0))),
    beatBytes  = beatBytes,
    minLatency = 1)))

  lazy val module = new AxinsimpleModuleImp(this)
}

class AxinsimpleModuleImp(outer: AxinsimpleWrapper) extends LazyModuleImp(outer) {
  val (inPort, _) = outer.node.in(0)

  val axinsimple_ram = Module(new Axinsimple(32))
  axinsimple_ram.io.rst := reset.asBool
  axinsimple_ram.io.clk := clock

  axinsimple_ram.io.s_axi_awid := inPort.aw.bits.id
  axinsimple_ram.io.s_axi_awaddr := inPort.aw.bits.addr
  axinsimple_ram.io.s_axi_awlen := inPort.aw.bits.len
  axinsimple_ram.io.s_axi_awsize := inPort.aw.bits.size
  axinsimple_ram.io.s_axi_awburst := inPort.aw.bits.burst
  axinsimple_ram.io.s_axi_awlock := inPort.aw.bits.lock
  axinsimple_ram.io.s_axi_awcache := inPort.aw.bits.cache
  axinsimple_ram.io.s_axi_awprot := inPort.aw.bits.prot
  axinsimple_ram.io.s_axi_awvalid := inPort.aw.valid
  inPort.aw.ready := axinsimple_ram.io.s_axi_awready

  axinsimple_ram.io.s_axi_wdata := inPort.w.bits.data
  axinsimple_ram.io.s_axi_wstrb := inPort.w.bits.strb
  axinsimple_ram.io.s_axi_wlast := inPort.w.bits.last
  axinsimple_ram.io.s_axi_wvalid := inPort.w.valid
  inPort.w.ready := axinsimple_ram.io.s_axi_wready

  inPort.b.bits.id := axinsimple_ram.io.s_axi_bid
  inPort.b.bits.resp := axinsimple_ram.io.s_axi_bresp
  inPort.b.valid := axinsimple_ram.io.s_axi_bvalid
  axinsimple_ram.io.s_axi_bready := inPort.b.ready

  axinsimple_ram.io.s_axi_arid := inPort.ar.bits.id
  axinsimple_ram.io.s_axi_araddr := inPort.ar.bits.addr
  axinsimple_ram.io.s_axi_arlen := inPort.ar.bits.len
  axinsimple_ram.io.s_axi_arsize := inPort.ar.bits.size
  axinsimple_ram.io.s_axi_arburst := inPort.ar.bits.burst
  axinsimple_ram.io.s_axi_arlock := inPort.ar.bits.lock
  axinsimple_ram.io.s_axi_arcache := inPort.ar.bits.cache
  axinsimple_ram.io.s_axi_arprot := inPort.ar.bits.prot
  axinsimple_ram.io.s_axi_arvalid := inPort.ar.valid
  inPort.ar.ready := axinsimple_ram.io.s_axi_arready

  inPort.r.bits.id := axinsimple_ram.io.s_axi_rid
  inPort.r.bits.data := axinsimple_ram.io.s_axi_rdata
  inPort.r.bits.resp := axinsimple_ram.io.s_axi_rresp
  inPort.r.bits.last := axinsimple_ram.io.s_axi_rlast
  inPort.r.valid := axinsimple_ram.io.s_axi_rvalid
  axinsimple_ram.io.s_axi_rready := inPort.r.ready
}

// DOC include start: Axinsimple blackbox
class Axinsimple(val w: Int) extends BlackBox with HasBlackBoxResource
{
  val io = IO(new Bundle {
    val rst = Input(Bool())
    val clk = Input(Clock())

    val s_axi_awid = Input(UInt((8).W))
    val s_axi_awaddr = Input(UInt((16).W))
    val s_axi_awlen = Input(UInt((8).W))
    val s_axi_awsize = Input(UInt((3).W))
    val s_axi_awburst = Input(UInt((2).W))
    val s_axi_awlock = Input(Bool())
    val s_axi_awcache = Input(UInt((4).W))
    val s_axi_awprot = Input(UInt((3).W))
    val s_axi_awvalid = Input(Bool())
    val s_axi_awready = Output(Bool())

    val s_axi_wdata = Input(UInt((32).W))
    val s_axi_wstrb = Input(UInt((4).W))
    val s_axi_wlast = Input(Bool())
    val s_axi_wvalid = Input(Bool())
    val s_axi_wready = Output(Bool())

    val s_axi_bid = Output(UInt((8).W))
    val s_axi_bresp = Output(UInt((2).W))
    val s_axi_bvalid = Output(Bool())
    val s_axi_bready = Input(Bool())

    val s_axi_arid = Input(UInt((8).W))
    val s_axi_araddr = Input(UInt((16).W))
    val s_axi_arlen = Input(UInt((8).W))
    val s_axi_arsize = Input(UInt((3).W))
    val s_axi_arburst = Input(UInt((2).W))
    val s_axi_arlock = Input(Bool())
    val s_axi_arcache = Input(UInt((4).W))
    val s_axi_arprot = Input(UInt((3).W))
    val s_axi_arvalid = Input(Bool())
    val s_axi_arready = Output(Bool())

    val s_axi_rid = Output(UInt((8).W))
    val s_axi_rdata = Output(UInt((32).W))
    val s_axi_rresp = Output(UInt((2).W))
    val s_axi_rlast = Output(Bool())
    val s_axi_rvalid = Output(Bool())
    val s_axi_rready = Input(Bool())
  })

  addResource("/vsrc/Axinsimple.v")
}

trait CanHavePeripheryAxinsimple { this: BaseSubsystem =>
  private val portName = "axi-dev2-ram"

  val ansimple = p(AxinsimpleKey) match {
    case Some(params) => {
      val ansimple = LazyModule(new AxinsimpleWrapper(pbus.beatBytes)(p))
      pbus.coupleTo(portName){
        ansimple.node :=
        AXI4UserYanker(Some(1)) :=
        TLToAXI4 () :=
        // toVariableWidthSlave doesn't use holdFirstDeny, which TLToAXI4() needsx
        TLFragmenter(pbus.beatBytes, pbus.blockBytes, holdFirstDeny = true) :*= _
      }
      Some(ansimple)
    }
    case None => None
  }
}


// DOC include start: WithAxinsimple
class WithAxinsimple(base: BigInt, size: BigInt) extends Config((site, here, up) => {
  case AxinsimpleKey => Some(AxinsimpleConfig(base=base, size=size))
})
// DOC include end: WithAxinsimple

