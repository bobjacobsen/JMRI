print ("Python 3 tests!")

import jmri as jmri
import java

# access constants

if (jmri.Turnout.THROWN != 4) : raise AssertionError('Constant THROWN not right')
