import collections

def dfs(pair, visited):
    if pair in visited:
        return
    visited.add(pair)
    src, mask = pair



'''
Parse fibs into flash format
'''
def parse():
    # files = ["r0"]
    link_dict = {}
    port_to_name = {}

    f = open("testnet.config", 'r')
    for line in f.readlines()[1:]:
        line_parts = line.split()

        if not len(line_parts):
            continue

        for part in line_parts[1:]:
            l1, l2 = part.split(":")
            if l1 and l2:
                link_dict[l1] = l2
                port_to_name[l1] = line_parts[0]

    print(link_dict)
    print(port_to_name)
    f.close()

    exits = []
    for port in link_dict:
        if link_dict[port] not in link_dict:
            exits.append((port, link_dict[port], port_to_name[port]))

    for ex in exits:
        link_dict.pop(ex[0])

    print(".,,,,,,")
    print(exits)

    port_to_pair = {}
    for l1, l2 in link_dict.items():
        port_to_pair[l1] = [l1, l2]

    print(port_to_pair)

    raw = []
    for i in range(4):
        f_name = "minir" + str(i) + ".fib"

        f = open(f_name, 'r')

        for line in f.readlines()[1:]:
            line_parts = line.split()

            # assert line_parts == 8

            src = line_parts[0]
            mask = line_parts[2]
            port = line_parts[-1]

            raw.append([src, mask, port])

            if port not in port_to_pair:
                print(port)
            # adjacency_list[src].append()

            # print(dest, mask, iface)

        f.close()


    name_to_ip = {}
    ip_to_name = {}
    ip_to_mask = {}
    for ip, mask, port in raw:
        name = port_to_name[port]
        name_to_ip[name] = ip
        ip_to_name[ip] = name
        ip_to_mask[ip] = mask

    print("xxxxx")
    print(name_to_ip)

    iface_to_src = {}
    for src, mask, iface in raw:
        iface_to_src[iface] = src

    print(iface_to_src)

    port_to_pair_ip = collections.defaultdict(list)
    for port in port_to_pair.keys():
        for p in port_to_pair[port]:
            if p in iface_to_src:
                port_to_pair_ip[port].append(iface_to_src[p])
            else:
                port_to_pair_ip[port].append(".")
        # port_to_pair_ip[port] =
    print(port_to_pair_ip)

    pair_to_port = {}
    for port in port_to_pair_ip.keys():
        pair_to_port[tuple(port_to_pair_ip[port])] = port

    print(pair_to_port)

    adjacency_list = collections.defaultdict(list)
    for src, mask, iface in raw:
        if iface in link_dict:
            adjacency_list[src].append(iface_to_src[link_dict[iface]])


    print("----------")
    print(raw)
    print(link_dict)
    print(adjacency_list)
    print("________")


    visisted = set()
    pred = {}
    def bfs(node):
        queue = collections.deque()

        queue.append(node)
        visisted.add(node)

        while queue:
            curr = queue.popleft()

            for n in adjacency_list[curr]:
                if n not in visisted:
                    visisted.add(n)
                    pred[n] = curr
                    queue.append(n)


    device_to_paths = {}
    for node in adjacency_list:
        paths_to_take = {}
        visisted = set()
        pred = {}
        bfs(node)
        for p in pred:
            curr = p
            while pred[curr] != node:
                curr = pred[curr]

            paths_to_take[p] = [pair_to_port[(node, curr)], pair_to_port[(curr, node)]]
        for pair in exits:
            egress, ingress, name = pair
            curr = name_to_ip[name]
            if curr in pred:
                while pred[curr] != node:
                    curr = pred[curr]
                paths_to_take[name_to_ip[name]] = [pair_to_port[(node, curr)], pair_to_port[(curr, node)]]
            else:
                paths_to_take[name_to_ip[name]] = [egress, ingress]
        device_to_paths[ip_to_name[node]] = paths_to_take


    print(device_to_paths)
    print(exits)

        # This code generates only for the exit code
        # for pair in exits:
        #     _port, name = pair
        #     curr = name_to_ip[name]
        #     if curr in pred:
        #         while pred[curr] != node:
        #             curr = pred[curr]
        #         paths_to_take[ip_to_name[node]].append(pair_to_port[(node, curr)])
        #     else:
        #         paths_to_take[ip_to_name[node]].append(_port)

        # for ele in pred:
        #     curr = ele
        #     while pred[curr] != pair:
        #         curr = pred[curr]

            # paths_to_take[(pair, curr)].append(ele)
        # curr =
        # paths_to_take[pair_to_port[(pair, curr)]].append(ele)



    def subnet_mask_to_cidr(mask):
        # Split the mask into octets and convert each to binary
        binary_str = ''.join([bin(int(octet))[2:].zfill(8) for octet in mask.split('.')])
        # Count the number of consecutive 1s from the start
        return binary_str.count('1')

    for device in device_to_paths:
        f = open("new"+device+"ap.fib", 'w+')
        f.write("Routing table: "+device+".inet\n")
        f.write("Internet:\n")
        f.write("Destination        Type   Next hop       Type Index NhRef Netif\n")

        for idx, dest in enumerate(device_to_paths[device]):
            mask = ip_to_mask[dest]
            prefix = subnet_mask_to_cidr(mask)
            dest_string = (dest+"/"+str(prefix)).ljust(len("Destination        "))

            type_string = "dest".ljust(len("Type   "))

            egress, ingress = device_to_paths[device][dest]
            hop_string = (ingress).ljust(len("Next hop       "))

            type_string2 = "dest".ljust(len("dest  "))

            index_string = ("10" + str(idx)).ljust(len("101    "))

            nhref_string = "1   "

            netif_string = egress

            f.write(dest_string+type_string+hop_string+type_string2+index_string+nhref_string+netif_string+"\n")
        f.close()

if __name__ == "__main__":
    parse()