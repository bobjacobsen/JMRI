print ("Python 3 rules!")

import jmri as jmri
import java

# access constants

print ( jmri.Version.NON_OFFICIAL )

# access InstanceManager to get manager
smc = java.type('jmri.SensorManager')
sm = jmri.InstanceManager.getNullableDefault(smc)

# use that manager to affect JMRI
IS1 = sm.provideSensor("IS1")

# but really, should use the simpler syntax
IS2 = sensors.provideSensor("IS2")

# check constants
print(jmri.Turnout.THROWN)
print(THROWN)

# extending a class
from jmri.jmrit.automat import AbstractAutomaton

class Automat(AbstractAutomaton) :
    def init(self) :
        print ("init")
    def handle(self) :
        print ("handle")
        return False

Automat().start()

# prep to do this again
print (IS1)
persistanceCheck = 27
