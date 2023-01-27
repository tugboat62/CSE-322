import sys

size = 50
mesType = 'flows'

if len(sys.argv) == 3 :
    mesType = sys.argv[1]
    size = int(sys.argv[2])

received_packets = 0
sent_packets = 0
dropped_packets = 0
total_delay = 0
received_bytes = 0

start_time = 1000000
end_time = 0

# constants
header_bytes = 20

# Using readline()
file1 = open('trace.tr', 'r')
file2 = open('data.txt', 'a+')
count = 0
sent_time = {}


while True:
    count += 1
    line = file1.readline()
    if not line:
        break
    params = line.split()
    event = params[0]
    time_sec = float(params[1])
    node = params[2].strip("_")
    layer = params[3]
    packet_id = params[5]
    packet_type = params[6]


    if start_time > time_sec:
        start_time = time_sec

    if layer == "AGT" and packet_type == "cbr" :
        if event == "s" :
            sent_time[packet_id] = time_sec
            sent_packets += 1

        elif event == "r" :
            packet_bytes = float(params[7])
            delay = time_sec - sent_time[packet_id]
            total_delay += delay
            bytesNo = packet_bytes - header_bytes
            received_bytes += bytesNo
            received_packets += 1

    if packet_type == "cbr" and event == "D" :
        dropped_packets += 1


file1.close()
sent_time = time_sec
simulation_time = end_time - start_time
throughput = (received_bytes * 8) / simulation_time
avgDelay = (total_delay / received_packets)
delRate = (received_packets / sent_packets)
dropRatio = (dropped_packets / sent_packets)
line = f"{mesType} {size} {throughput} {avgDelay} {delRate} {dropRatio}\n"
file2.write(line)

file2.close()