package main

import (
	"fmt"
	"go/parser"
	"go/token"
	"log"
	"os"
)

func main() {
	if len(os.Args) <= 1 {
		log.Fatal("No file to parse imports from provided.")
	}
	fileSet := token.NewFileSet()
	f, err := parser.ParseFile(fileSet, os.Args[1], nil, parser.ImportsOnly)
	if err != nil {
		log.Fatalf("Could not parse imports. Caused: %v", err)
	}
	for _, importName := range f.Imports {
		fmt.Println(importName.Path.Value)
	}
}
