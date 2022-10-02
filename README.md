# jmorsec

This is a simple jbang script to encode or decode an input to ist associated morse code input/output.


### Usage

#### Encode
Command to encode a string:

```java
j! jmorsec.java SOS
```

Output:

```
The input 'SOS' encoded is : '*** --- ***'%
```

#### Decode

Command to decode a morse string:

```java
j! jmorse.java -d '*** --- ***'
```

Output:

```
The input '*** --- ***' decoded is : 'SOS'%
```


### Help

If some help is needed try following command:

```java
j! jmorse.java -h
```
