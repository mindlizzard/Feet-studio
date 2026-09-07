import React, { useState, useEffect } from 'react';
import ControlPanel from './components/ControlPanel';
import { DesignState, INITIAL_STATE } from './types';
import { generateImage, generateSocialCaption } from './services/geminiService';

const App: React.FC = () => {
  const [designState, setDesignState] = useState<DesignState>(INITIAL_STATE);
  const [generatedImage, setGeneratedImage] = useState<string | null>(null);
  const [caption, setCaption] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<string[]>([]);
  const [toast, setToast] = useState<string | null>(null);
  
  // Mobile UI State
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const handleStateChange = (updates: Partial<DesignState>) => {
    setDesignState(prev => ({ ...prev, ...updates }));
  };

  const handleGenerate = async () => {
    setIsLoading(true);
    setError(null);
    setCaption(null);
    
    try {
      const imgData = await generateImage(designState);
      setGeneratedImage(imgData);
      setHistory(prev => [imgData, ...prev]);
      
      // Generate caption in parallel-ish
      generateSocialCaption(designState).then(setCaption);
      
    } catch (err: any) {
      setError(err.message || "Fout bij genereren. Probeer het opnieuw.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownload = () => {
    if (!generatedImage) return;
    const link = document.createElement('a');
    link.href = generatedImage;
    // Save as PNG for "RAW"/Lossless quality
    link.download = `velvetsole-raw-${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setToast("PNG (Lossless) Gedownload! 💾");
  };

  const handleShare = async () => {
    if (!generatedImage) return;

    // 1. Always try to copy caption to clipboard first (UX for Instagram)
    if (caption) {
      try {
        await navigator.clipboard.writeText(caption);
        setToast("Caption gekopieerd naar klembord! 📋");
      } catch (err) {
        console.error("Clipboard failed", err);
      }
    } else {
        setToast("Afbeelding voorbereiden... ⏳");
    }

    // 2. Use Native Share if available (Mobile)
    if (navigator.share) {
      try {
        const response = await fetch(generatedImage);
        const blob = await response.blob();
        const file = new File([blob], 'velvetsole-ontwerp.png', { type: 'image/png' });
        
        await navigator.share({
          title: 'VelvetSole Ontwerp',
          text: caption || 'Bekijk mijn ontwerp! #velvetsole',
          files: [file]
        });
      } catch (err) {
        console.log('Share dismissed or failed', err);
      }
    } else {
      // Desktop Fallback
      if (!caption) setToast("Afbeelding klaar. Download om te delen.");
      else setToast("Caption gekopieerd! Download afbeelding om te posten. 📋");
    }
  };

  return (
    <div className="flex flex-col lg:flex-row h-screen bg-black overflow-hidden relative">
      
      {/* Toast Notification */}
      {toast && (
        <div className="absolute top-20 left-1/2 -translate-x-1/2 z-[60] bg-pink-600 text-white px-6 py-3 rounded-full shadow-2xl font-bold text-sm tracking-wide animate-bounce">
          {toast}
        </div>
      )}

      {/* Mobile Top Bar */}
      <div className="lg:hidden h-14 bg-gray-950 border-b border-gray-800 flex items-center justify-between px-4 z-20 flex-shrink-0">
          <div className="text-white font-light text-lg">Velvet<span className="text-pink-500 font-bold">Sole</span></div>
          <button 
            onClick={() => setIsMobileMenuOpen(true)}
            className="text-xs uppercase bg-gray-800 text-pink-500 px-3 py-1.5 rounded border border-gray-700 hover:bg-gray-700 font-bold tracking-wider"
          >
            Aanpassen
          </button>
      </div>

      {/* Control Panel (Responsive: Sidebar on Desktop, Slide-over on Mobile) */}
      <div className={`
        fixed inset-0 z-40 bg-gray-950/95 backdrop-blur-md transition-transform duration-300 lg:relative lg:translate-x-0 lg:bg-transparent lg:w-96 lg:block
        ${isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
          <div className="h-full w-full">
            <ControlPanel 
                state={designState} 
                onChange={handleStateChange} 
                onGenerate={handleGenerate}
                isGenerating={isLoading}
                onCloseMobile={() => setIsMobileMenuOpen(false)}
            />
          </div>
      </div>

      {/* Main Canvas Area */}
      <main className="flex-1 relative flex flex-col h-full overflow-hidden">
        
        {/* Desktop Top Bar (Hidden on Mobile) */}
        <div className="hidden lg:flex h-16 border-b border-gray-800 bg-gray-950/50 backdrop-blur items-center justify-between px-8 z-10 flex-shrink-0">
            <div className="text-gray-400 text-xs uppercase tracking-widest">
                Render Viewport (NL)
            </div>
            <div className="flex items-center space-x-4">
                <span className="text-xs text-gray-500">API: {designState.useProModel ? 'Gemini 3 Pro (8K RAW Mode)' : 'Gemini 2.5 Flash'}</span>
            </div>
        </div>

        {/* Image Display */}
        <div className="flex-1 flex items-center justify-center bg-gray-900 relative p-4 lg:p-8 overflow-hidden">
            {/* Grid Pattern Background */}
            <div className="absolute inset-0 opacity-10 pointer-events-none" style={{ backgroundImage: 'radial-gradient(#4b5563 1px, transparent 1px)', backgroundSize: '20px 20px' }}></div>
            
            {generatedImage ? (
                <div className="relative w-full h-full flex items-center justify-center group">
                    <img 
                        src={generatedImage} 
                        alt="Gegenereerd Ontwerp" 
                        className="max-h-full max-w-full object-contain shadow-2xl shadow-black rounded-lg" 
                    />
                    
                    {/* Action Buttons (Download & Share) */}
                    <div className="absolute top-4 right-4 flex space-x-3 z-30">
                        <button 
                            onClick={handleShare}
                            className="bg-black/40 hover:bg-pink-600 text-white p-3 rounded-full backdrop-blur-md transition-all border border-white/10 shadow-lg group-hover:scale-105"
                            title="Delen op Instagram (Kopieer Caption & Deel)"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
                            </svg>
                        </button>
                        <button 
                            onClick={handleDownload}
                            className="bg-black/40 hover:bg-pink-600 text-white p-3 rounded-full backdrop-blur-md transition-all border border-white/10 shadow-lg group-hover:scale-105"
                            title="PNG (RAW/Lossless) Downloaden"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                            </svg>
                        </button>
                    </div>

                    {/* Social Modal Overlay */}
                    <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black via-black/80 to-transparent p-6 lg:translate-y-full lg:group-hover:translate-y-0 transition-transform duration-300 pointer-events-none lg:pointer-events-auto">
                        <h4 className="text-pink-400 text-xs font-bold uppercase mb-2">Social Export</h4>
                        <p className="text-gray-200 text-sm italic font-serif mb-4 select-text cursor-text pointer-events-auto">
                            {caption || "Caption genereren..."}
                        </p>
                    </div>
                </div>
            ) : (
                <div className="text-center text-gray-600">
                    <div className="mb-4 text-5xl lg:text-6xl opacity-20">📸</div>
                    <p className="uppercase tracking-widest text-xs lg:text-sm">Wachten op input...</p>
                </div>
            )}

            {isLoading && (
                <div className="absolute inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center z-50">
                    <div className="text-center">
                        <div className="w-12 h-12 border-4 border-pink-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                        <p className="text-pink-500 text-xs uppercase tracking-widest animate-pulse">8K Rendering...</p>
                    </div>
                </div>
            )}
            
            {error && (
                <div className="absolute bottom-8 right-8 left-8 lg:left-auto lg:max-w-sm bg-red-900/90 border border-red-500 text-white p-4 rounded shadow-xl z-50">
                    <p className="text-sm font-bold">Systeem Fout</p>
                    <p className="text-xs mt-1 opacity-80">{error}</p>
                </div>
            )}
        </div>

        {/* History Strip (Bottom) */}
        {history.length > 0 && (
            <div className="h-20 lg:h-24 bg-gray-950 border-t border-gray-800 flex items-center px-4 space-x-2 overflow-x-auto flex-shrink-0 scrollbar-hide">
                {history.map((img, idx) => (
                    <button 
                        key={idx} 
                        onClick={() => setGeneratedImage(img)}
                        className={`h-14 w-14 lg:h-16 lg:w-16 rounded overflow-hidden border-2 transition-all flex-shrink-0 ${generatedImage === img ? 'border-pink-500 opacity-100' : 'border-gray-700 opacity-50 hover:opacity-80'}`}
                    >
                        <img src={img} alt="" className="w-full h-full object-cover" />
                    </button>
                ))}
            </div>
        )}
      </main>
    </div>
  );
};

export default App;