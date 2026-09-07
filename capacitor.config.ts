import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.mindlizzard.feetstudio',
  appName: 'Feet Studio',
  webDir: 'dist',
  android: {
    allowMixedContent: false,
  },
};

export default config;
