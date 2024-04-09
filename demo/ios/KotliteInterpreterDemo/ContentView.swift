//
//  ContentView.swift
//  KotliteInterpreterDemo
//
//  Created by Sunny Chung on 9/4/2024.
//

import SwiftUI
import KotliteInterpreterDemoShared

private var demoScriptNames = Array(DemoKt.demoScripts.keys)

struct ContentView: View {
    @State private var lastSelectedExample = ""
    @State private var selectedExample = "Hello World"
    @State private var code = ""
    @State private var output = ""
    @State private var isRunning = false
    
    var body: some View {
        let _ = updateCodeIfNeeded()
        ScrollView {
            VStack(alignment: .leading) {
                HStack {
                    Picker("Example Code", selection: $selectedExample) {
                        ForEach(demoScriptNames, id: \.self) { key in
                            Text(key)
                        }
                    }.onChange(of: selectedExample) { oldValue, newValue in
                        updateCodeIfNeeded()
                    }
                    Spacer()
                    if (!isRunning) {
                        Button("Run") {
                            runCode()
                        }
                        .buttonStyle(.bordered)
                    } else {
                        ProgressView()
                    }
                }
                
                Text("Code").padding(EdgeInsets(top: 12, leading: 0, bottom: 0, trailing: 0))
                TextField("", text: $code, axis: .vertical)
                    .lineLimit(25, reservesSpace: true)
                    .background(.cyan)
                    .font(.system(size: 14, design: .monospaced))
                    .autocorrectionDisabled()
                
                Text("Output").padding(EdgeInsets(top: 12, leading: 0, bottom: 0, trailing: 0))
                TextField("", text: $output, axis: .vertical)
                    .lineLimit(10, reservesSpace: true)
                    .background(.gray.opacity(0.2))
                    .font(.system(size: 14, design: .monospaced))
                    .disabled(true)
                Spacer()
            }
        }
        .padding()
        .onAppear {
            updateCodeIfNeeded()
        }
    }
    
    func updateCodeIfNeeded() {
        if (selectedExample != lastSelectedExample) {
            print("update code")
            print(DemoKt.demoScripts[selectedExample]!)
            code = DemoKt.demoScripts[selectedExample]!
            lastSelectedExample = selectedExample
        }
    }
    
    func runCode() {
        isRunning = true
        Task {
            output = await execute()
            isRunning = false
        }
    }
    
    func execute() async -> String {
        return DemoKt.interpretKotlite(code: code)
    }
}

#Preview {
    ContentView()
}
