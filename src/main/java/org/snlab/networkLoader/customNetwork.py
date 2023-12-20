from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import Node
from mininet.log import setLogLevel, info
from mininet.cli import CLI


class LinuxRouter( Node ):
    "A Node with IP forwarding enabled."

    # pylint: disable=arguments-differ
    def config( self, **params ):
        super( LinuxRouter, self).config( **params )
        # Enable forwarding on the router
        self.cmd( 'sysctl net.ipv4.ip_forward=1' )

    def terminate( self ):
        self.cmd( 'sysctl net.ipv4.ip_forward=0' )
        super( LinuxRouter, self ).terminate()


class NetworkTopo( Topo ):
    "A LinuxRouter connecting three IP subnets"

    # pylint: disable=arguments-differ
    def build( self, **_opts ):

        r0 = self.addNode('r0', cls=LinuxRouter, ip='192.168.1.1/24')
        r1 = self.addNode('r1', cls=LinuxRouter, ip='175.203.1.1/24')
        r2 = self.addNode('r2', cls=LinuxRouter, ip='163.203.1.1/24')
        r3 = self.addNode('r3', cls=LinuxRouter, ip='155.155.1.1/24')

        h1 = self.addHost( 'h1', ip="457.212.1.1/24", defaultRoute='via 155.155.1.1')
        s1 = self.addSwitch('s1')

        self.addLink(r0, r1,
                     intfName1='r0-eth0', intfName2='r1-eth0',
                     params1={'ip': '192.168.1.1/24'}, params2={'ip': '175.203.1.1/24'})
        self.addLink(r1, r2,
                     intfName1='r1-eth1', intfName2='r2-eth0',
                     params1={'ip': '175.203.1.1/24'}, params2={'ip': '163.203.1.1/24'})
        self.addLink(r2, r3,
                     intfName1='r2-eth1', intfName2='r3-eth0',
                     params1={'ip': '163.203.1.1/24'}, params2={'ip': '155.155.1.1/24'})

        self.addLink(s1, r3, intfName2='r3-eth1', params2={'ip': '481.123.1.1/24'})

        self.addLink(s1, h1)


def run():
    "Test linux router"
    topo = NetworkTopo()
    net = Mininet( topo=topo,
                   waitConnected=True )  # controller is used by s1-s3
    net.start()
    info( '*** Routing Table on Router:\n' )
    info( net[ 'r0' ].cmd( 'route' ) )
    CLI( net )
    net.stop()


if __name__ == '__main__':
    setLogLevel( 'info' )
    run()