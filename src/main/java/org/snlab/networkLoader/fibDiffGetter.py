import difflib

def file_diff(fib1, fib2):

    with open(fib1, 'r') as f1, open(fib2, 'r') as f2:
        diff = difflib.unified_diff(f1.readlines(), f2.readlines(), fromfile=fib1, tofile=fib2, n=0)

    return diff


if __name__ == "__main__":
    fib1 = "r0ap"
    fib2 = "r0ap_change"

    diff = file_diff(fib1, fib2)

    lines = list(diff)[2:]
    added = [line[1:] for line in lines if line[0] == '+']
    removed = [line[1:] for line in lines if line[0] == '-']

    print("added")
    for line in added:
        if line not in removed:
            print(line)

    print("removed")
    for line in removed:
        if line not in added:
            print(line)
