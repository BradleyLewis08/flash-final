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
            exits.append((port, port_to_name[port]))

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
        f_name = "r" + str(i) + ".fib"

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

    paths_to_take = collections.defaultdict(list)
    for node in adjacency_list:
        visisted = set()
        pred = {}
        bfs(node)
        for pair in exits:
            _port, name = pair
            curr = name_to_ip[name]
            if curr in pred:
                while pred[curr] != node:
                    curr = pred[curr]
                paths_to_take[ip_to_name[node]].append(pair_to_port[(node, curr)])
            else:
                paths_to_take[ip_to_name[node]].append(_port)

        # for ele in pred:
        #     curr = ele
        #     while pred[curr] != pair:
        #         curr = pred[curr]

            # paths_to_take[(pair, curr)].append(ele)
        # curr =
        # paths_to_take[pair_to_port[(pair, curr)]].append(ele)

    print(paths_to_take)


    def subnet_mask_to_cidr(mask):
        # Split the mask into octets and convert each to binary
        binary_str = ''.join([bin(int(octet))[2:].zfill(8) for octet in mask.split('.')])
        # Count the number of consecutive 1s from the start
        return binary_str.count('1')

    for device in paths_to_take:
        f = open(device+"ap", 'w+')

        ip = name_to_ip[device]
        mask = ip_to_mask[ip]

        prefix = subnet_mask_to_cidr(mask)

        port = paths_to_take[device][0]

        f.write(f'fw 155.155.1.0 {prefix} {port}')

        f.close()



if __name__ == "__main__":
    parse()