#!/usr/bin/env groovy

def primitives = [
	"int","byte","short","long","float","double","char",
	"java.lang.String",
	"java.lang.Object",
	"boolean","void",
	"java.lang.BigDecimal",
	"java.lang.BigInteger",
	"java.lang.Byte",
	"java.lang.Double",
	"java.lang.Float",
	"java.lang.Integer",
	"java.lang.Long",
	"java.lang.Short",
	"java.lang.Boolean",
	"java.lang.System",
	"java.lang.Exception"
	]

new File("jdk.txt").splitEachLine("\t"){
	if(!(it[1] in primitives)) println it[0]+"\t"+it[1]+"\t"+it[2]
}
