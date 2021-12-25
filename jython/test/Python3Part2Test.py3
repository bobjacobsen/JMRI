# see if same context using settings from 1st script
if (persistanceCheck != 27) : raise AssertionError('Variable not persistant')
if (sensors.getSensor("IS1") == None) : raise AssertionError('Sensor not persistant')

