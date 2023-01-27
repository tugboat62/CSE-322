# simulator
set ns [new Simulator]


# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy/802_15_4 ;# network interface type
set val(mac)          Mac/802_15_4             ;# MAC type
set val(rp)           DSDV                     ;# ad-hoc routing protocol 
set val(nn)           50                       ;# number of mobilenodes
set val(nf)           20                       ;# number of flows
set val(grid)         500                      ;# grid size 
# =======================================================================

# trace file
set trace_file [open trace.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open animation.nam w]

# take arguments from command line
if { $argc == 3 } {
    puts "inside arguments"
    set val(grid) [lindex $argv 0]
    set val(nn)   [lindex $argv 1]
    set val(nf)   [lindex $argv 2]
    puts $val(grid)
    puts $val(nn)
    puts $val(nf)
}

$ns namtrace-all-wireless $nam_file $val(grid) $val(grid)

# topology: to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $val(grid) $val(grid) ;# 500m x 500m area


# general operation director for mobilenodes
create-god $val(nn)


# node configs
# ======================================================================

# $ns node-config -addressingType flat or hierarchical or expanded
#                  -adhocRouting   DSDV or DSR or TORA
#                  -llType	   LL
#                  -macType	   Mac/802_11
#                  -propType	   "Propagation/TwoRayGround"
#                  -ifqType	   "Queue/DropTail/PriQueue"
#                  -ifqLen	   50
#                  -phyType	   "Phy/WirelessPhy"
#                  -antType	   "Antenna/OmniAntenna"
#                  -channelType    "Channel/WirelessChannel"
#                  -topoInstance   $topo
#                  -energyModel    "EnergyModel"
#                  -initialEnergy  (in Joules)
#                  -rxPower        (in W)
#                  -txPower        (in W)
#                  -agentTrace     ON or OFF
#                  -routerTrace    ON or OFF
#                  -macTrace       ON or OFF
#                  -movementTrace  ON or OFF

# ======================================================================

$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -topoInstance $topo \
                -channelType $val(chan) \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF


array set nodes {}

# create nodes
for {set i 0} {$i < $val(nn) } {incr i} {
    set node($i) [$ns node]
    set speed [expr 1.0 + rand()*4.0] 
    $node($i) random-motion 0       ;# enable random motion
    
    set xx [expr rand()*$val(grid)]
    set yy [expr rand()*$val(grid)]
    
    while {[info exists nodes(xx,yy)]} {
        set xx [expr rand()*$val(grid)]
        set yy [expr rand()*$val(grid)]
    } 

    $node($i) set X_ $xx
    $node($i) set Y_ $yy
    $node($i) set Z_ 0.0

    set nodes($xx,$yy) 1

    $ns initial_node_pos $node($i) 20
    set dest_x [expr rand()*$val(grid)]
    set dest_y [expr rand()*$val(grid)]
    # puts "node $i"
    $ns at 1.0 "$node($i) setdest $dest_x $dest_y $speed"
} 

unset nodes

# create flows
for {set i 0} {$i < $val(nf)} {incr i} {
    set src [expr {int(rand()*$val(nn))}]
    set dest [expr {int(rand()*$val(nn))}]
    while {$src == $dest} {
        set dest [expr {int(rand()*$val(nn))}]
    }
    
    # Traffic config
    # create agent
    set udp [new Agent/UDP]
    $udp set class_ 2
    # attach to nodes
    $ns attach-agent $node($src) $udp
    set null [new Agent/Null]
    $ns attach-agent $node($dest) $null
    # connect agents
    $ns connect $udp $null
    $udp set fid_ $i

    # Traffic generator
    set cbr [new Application/Traffic/CBR]
    # attach to agent
    $cbr attach-agent $udp
    $cbr set type_ CBR
    $cbr set packetSize_ 1000
    $cbr set rate_ 1mb
    $cbr set random_ false

    # puts "Flow $i"
    
    # start traffic generation
    $ns at 1.0 "$cbr start"
}



# End Simulation

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 40.0 "$node($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}

$ns at 40.0001 "finish"
$ns at 40.0002 "halt_simulation"


# Run simulation
puts "Simulation starting"
$ns run

