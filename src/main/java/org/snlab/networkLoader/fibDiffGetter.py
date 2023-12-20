import difflib

def file_diff(fib1, fib2):

    with open(fib1, 'r') as f1, open(fib2, 'r') as f2:
        diff = difflib.unified_diff(f1.readlines(), f2.readlines(), fromfile=fib1, tofile=fib2)