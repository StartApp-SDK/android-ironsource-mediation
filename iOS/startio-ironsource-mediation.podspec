Pod::Spec.new do |spec|

  spec.name         = "startio-ironsource-mediation"
  spec.version      = "1.0.0"
  spec.summary      = "Start.io <-> ironSource iOS Mediation Adapter."

  spec.description  = <<-DESC
  Using this adapter you will be able to integrate Start.io SDK via ironSource mediation
                   DESC

  spec.homepage     = "https://www.start.io"
  spec.license      = { :type => "Apache License, Version 2.0", :file => "LICENSE" }
  spec.author       = { "iOS Dev" => "iosdev@startapp.com" }
  
  spec.platform     = :ios, "10.0"
  spec.source       = { :git => "https://github.com/StartApp-SDK/ironsource-mediation/tree/master/iOS", :tag => spec.version.to_s }
  spec.source_files = "StartioIronSourceMediation/*.{h,m}"

  spec.frameworks   = "Foundation", "UIKit"

  spec.requires_arc = true
  spec.static_framework = true

  spec.user_target_xcconfig = { 'ONLY_ACTIVE_ARCH' => 'YES' } 
  
  spec.dependency "IronSourceSDK", "~> 7.2.1.1"
  spec.dependency "StartAppSDK", "~> 4.7.0"

end
