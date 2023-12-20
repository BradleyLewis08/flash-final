def parse_file(config_filename):
    # Replace 'your_file.txt' with your file's name
    ports_to_device = {}
    # First pass to assign ports to devices
    with open(config_filename, 'r') as config_file:
        idx = 0
        next(config_file)
        for line in config_file:
            line = line.strip()
            tokens = line.split(" ")
            device = tokens[0]
            links = tokens[1:]
            for link in links:
                device_port = link.split(":")[0]
                if device_port not in ports_to_device:
                    ports_to_device[device_port] = device
    # Second pass to create the topology
    print(ports_to_device)
    added_links = []
    with open(config_filename, 'r') as config_file:
        with open("topology.out", 'w') as topology_file:
            idx = 0
            for line in config_file:
                if idx == 0:
                    # Copy devices over
                    topology_file.write(line)
                else:
                    line = line.strip()
                    tokens = line.split(" ")
                    device = tokens[0]
                    links = tokens[1:]
                    for link in links:
                        # Split the link by colon, sort the two devices, and then add the link if it hasn't been added
                        link_parts = link.split(":")
                        if "".join(sorted(link_parts)) in added_links:
                            continue
                        device_port = link.split(":")[0]
                        other_device_port = link.split(":")[1]
                        other_device = ports_to_device[other_device_port]
                        topology_file.write(
                            device + " " + device_port + " " + other_device + " " + other_device_port + "\n")
                        added_links.append("".join(sorted(link_parts)))
                idx += 1


parse_file("testnet.config")
