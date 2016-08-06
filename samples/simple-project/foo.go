package main

import (
	"fmt"
	"github.com/echocat/gradle-golang-plugin/samples/simple-project/bar"
)

var artifactId string

func main() {
	fmt.Println(artifactId)
	bar.Bar()
}
