## SPARQL Operators
Note: All operators are case insensitive
- **Mandatory**: ```SELECT, WHERE```
- **Optional**: ```LIMIT, UNION, FILTER```
	- LIMIT - Only accepts **Integer** value
	- FILTER
		 - **Logical:** ```!, &&, ||```
		 - **Comparison:** ```<, >, = or ==, >=, <=, !=```
		 - **SPARQL Tests:** ```isURI, isBlank, isLiteral```
		 - **SPARQL Accessors:** ```lang, datatype```

## FILTER

#### One-Line
```    
FILTER (?X == <http://www.Department6.University0.edu/UndergraduateStudent103>)
```

### Multi-Line
```    
FILTER (
    ?X == <http://www.Department6.University0.edu/UndergraduateStudent103> &&
    ?Z = <http://www.Department6.University0.edu/Course1>
)
```
