'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { Globe, LayoutGrid, ArrowRight, Sparkles, Zap, Shield } from 'lucide-react';
import { useWebHubStore } from '@/lib/store';
import { AnimatePresence, motion } from 'framer-motion';
import { cn } from '@/lib/utils';

const STEPS = [
  {
    icon: <Globe className="h-12 w-12" />,
    title: 'Welcome to WebHub',
    description: 'Your personal web workspace. Organize websites into workspaces, browse with persistent tabs, and take control of your online experience.',
    gradient: 'from-blue-500/20 to-purple-500/20',
  },
  {
    icon: <Zap className="h-12 w-12" />,
    title: 'Add Your First Site',
    description: 'Enter a URL below to create your first tab. You can add search engines, social media, productivity tools, or any website.',
    gradient: 'from-amber-500/20 to-orange-500/20',
  },
  {
    icon: <LayoutGrid className="h-12 w-12" />,
    title: 'Create Your First Workspace',
    description: 'Workspaces help you organize tabs by context — Work, Social, Finance, Entertainment, or anything you want.',
    gradient: 'from-green-500/20 to-emerald-500/20',
  },
  {
    icon: <Sparkles className="h-12 w-12" />,
    title: 'You\'re All Set!',
    description: 'WebHub is ready. Use the + button to add tabs, switch workspaces from the sidebar, and customize everything in Settings.',
    gradient: 'from-primary/20 to-primary/5',
  },
];

const QUICK_SITES = [
  { name: 'Google', url: 'https://www.google.com' },
  { name: 'GitHub', url: 'https://github.com' },
  { name: 'YouTube', url: 'https://www.youtube.com' },
];

export function OnboardingScreen() {
  const [step, setStep] = useState(0);
  const [url, setUrl] = useState('');
  const [workspaceName, setWorkspaceName] = useState('Personal');
  const { addTab, addWorkspace, updateSettings, selectWorkspace } = useWebHubStore();

  const handleFinish = async () => {
    if (workspaceName.trim()) {
      await addWorkspace(workspaceName.trim());
    }
    await updateSettings({ onboardingDone: true });
  };

  const handleAddSite = async (siteUrl: string) => {
    await addTab(siteUrl);
    setStep(2);
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-b from-background to-muted/30">
      <div className="w-full max-w-md">
        {/* Progress dots */}
        <div className="flex justify-center gap-2 mb-8">
          {STEPS.map((_, i) => (
            <div
              key={i}
              className={cn(
                'h-1.5 rounded-full transition-all',
                i === step ? 'w-8 bg-primary' : i < step ? 'w-1.5 bg-primary/50' : 'w-1.5 bg-muted'
              )}
            />
          ))}
        </div>

        <AnimatePresence mode="wait">
          <motion.div
            key={step}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.3 }}
          >
            <Card className="border-0 shadow-lg">
              <CardContent className="p-8">
                {/* Icon */}
                <div className={cn('w-20 h-20 rounded-2xl flex items-center justify-center mb-6 bg-gradient-to-br text-primary', STEPS[step].gradient)}>
                  {STEPS[step].icon}
                </div>

                <h1 className="text-2xl font-bold mb-2">{STEPS[step].title}</h1>
                <p className="text-muted-foreground mb-6 leading-relaxed">{STEPS[step].description}</p>

                {/* Step 1: Welcome */}
                {step === 0 && (
                  <Button className="w-full" onClick={() => setStep(1)}>
                    Get Started <ArrowRight className="h-4 w-4 ml-2" />
                  </Button>
                )}

                {/* Step 2: Add first site */}
                {step === 1 && (
                  <div className="space-y-3">
                    <Input
                      placeholder="Enter URL (e.g., google.com)"
                      value={url}
                      onChange={(e) => setUrl(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' && url.trim()) handleAddSite(url.trim());
                      }}
                      className="h-11"
                      autoFocus
                    />
                    <Button className="w-full" onClick={() => handleAddSite(url.trim())} disabled={!url.trim()}>
                      Add Site <ArrowRight className="h-4 w-4 ml-2" />
                    </Button>
                    <div className="flex items-center gap-2">
                      <div className="flex-1 h-px bg-border" />
                      <span className="text-xs text-muted-foreground">or try</span>
                      <div className="flex-1 h-px bg-border" />
                    </div>
                    <div className="flex gap-2">
                      {QUICK_SITES.map(s => (
                        <Button key={s.url} variant="outline" size="sm" className="flex-1" onClick={() => handleAddSite(s.url)}>
                          {s.name}
                        </Button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Step 3: Create workspace */}
                {step === 2 && (
                  <div className="space-y-3">
                    <Input
                      placeholder="Workspace name"
                      value={workspaceName}
                      onChange={(e) => setWorkspaceName(e.target.value)}
                      className="h-11"
                      autoFocus
                    />
                    <Button className="w-full" onClick={() => setStep(3)}>
                      Create Workspace <ArrowRight className="h-4 w-4 ml-2" />
                    </Button>
                    <Button variant="ghost" className="w-full" onClick={() => setStep(3)}>
                      Skip for now
                    </Button>
                  </div>
                )}

                {/* Step 4: Done */}
                {step === 3 && (
                  <div className="space-y-3">
                    <Button className="w-full" onClick={handleFinish}>
                      Start Browsing <Sparkles className="h-4 w-4 ml-2" />
                    </Button>
                    <div className="flex items-center gap-2 justify-center text-xs text-muted-foreground">
                      <Shield className="h-3 w-3" />
                      <span>Privacy-first. Your data stays local.</span>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>
        </AnimatePresence>

        {/* Skip button */}
        {step < 3 && (
          <button
            className="w-full text-center text-xs text-muted-foreground hover:text-foreground mt-4 py-2 transition-colors"
            onClick={handleFinish}
          >
            Skip onboarding
          </button>
        )}
      </div>
    </div>
  );
}