import { useState } from "react";
import { generateImage, fetchImageProviders } from "../api";

type ImageGeneratorProps = {
  token: string;
  projectId: number;
  cardGradient: string;
  onImageGenerated?: (imageUrl: string) => void;
};

type ImageProvider = {
  name: string;
  displayName: string;
  description: string;
  models: string[];
  sizes: string[];
};

export default function ImageGenerator({
  token,
  projectId,
  cardGradient,
  onImageGenerated,
}: ImageGeneratorProps) {
  const [prompt, setPrompt] = useState("");
  const [provider, setProvider] = useState<string>("dall-e");
  const [model, setModel] = useState<string>("dall-e-3");
  const [size, setSize] = useState<string>("1024x1024");
  const [quality, setQuality] = useState<number>(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [generationTime, setGenerationTime] = useState<number>(0);
  const [providers, setProviders] = useState<ImageProvider[]>([]);
  const [providersLoaded, setProvidersLoaded] = useState(false);

  // Load available providers on component mount
  useState(() => {
    if (!providersLoaded) {
      loadProviders();
    }
  });

  async function loadProviders() {
    try {
      const data = await fetchImageProviders(token);
      setProviders(data.providers);
      setProvidersLoaded(true);
    } catch (err) {
      console.error("Failed to load image providers:", err);
    }
  }

  async function handleGenerateImage(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setImageUrl(null);

    if (!prompt.trim()) {
      setError("Please enter an image description");
      return;
    }

    setLoading(true);
    const startTime = Date.now();

    try {
      const result = await generateImage(token, {
        prompt,
        projectId,
        provider,
        model: provider === "openrouter" ? model : undefined,
        size,
        quality: provider === "dall-e" ? quality : 0,
      });

      const elapsed = Date.now() - startTime;
      setGenerationTime(elapsed);
      setImageUrl(result.imageUrl);
      onImageGenerated?.(result.imageUrl);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to generate image"
      );
    } finally {
      setLoading(false);
    }
  }

  const selectedProvider = providers.find((p) => p.name === provider);
  const availableModels = selectedProvider?.models || [];
  const availableSizes = selectedProvider?.sizes || ["1024x1024"];

  // Update model if current selection is not available
  if (provider === "openrouter" && model !== availableModels[0]) {
    setModel(availableModels[0] || "stabilityai/stable-diffusion-3");
  }
  if (!availableSizes.includes(size)) {
    setSize(availableSizes[0] || "1024x1024");
  }

  return (
    <div className={`rounded-3xl border border-gray-200 ${cardGradient} p-4 shadow-sm`}>
      <h2 className="text-lg font-semibold text-slate-950 mb-4">
        🎨 AI Image Generator
      </h2>

      <form className="space-y-4" onSubmit={handleGenerateImage}>
        {/* Prompt Input */}
        <div>
          <label
            htmlFor="imagePrompt"
            className="block text-sm font-medium text-gray-700"
          >
            Image Description
          </label>
          <textarea
            id="imagePrompt"
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="Describe the image you want to generate... (e.g., 'A futuristic AI agent writing code at night with neon lights')"
            className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3 text-sm"
            rows={3}
            disabled={loading}
          />
          <p className="mt-1 text-xs text-gray-500">
            Be specific and descriptive for better results
          </p>
        </div>

        {/* Provider Selection */}
        <div>
          <label
            htmlFor="imageProvider"
            className="block text-sm font-medium text-gray-700"
          >
            Provider
          </label>
          <select
            id="imageProvider"
            value={provider}
            onChange={(e) => {
              setProvider(e.target.value);
              const newProvider = providers.find((p) => p.name === e.target.value);
              if (newProvider && newProvider.models.length > 0) {
                setModel(newProvider.models[0]);
              }
            }}
            className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3 text-sm"
            disabled={loading || providers.length === 0}
          >
            {providers.map((p) => (
              <option key={p.name} value={p.name}>
                {p.displayName}
              </option>
            ))}
          </select>
          {selectedProvider && (
            <p className="mt-1 text-xs text-gray-500">
              {selectedProvider.description}
            </p>
          )}
        </div>

        {/* Model Selection (OpenRouter only) */}
        {provider === "openrouter" && availableModels.length > 1 && (
          <div>
            <label
              htmlFor="imageModel"
              className="block text-sm font-medium text-gray-700"
            >
              Model
            </label>
            <select
              id="imageModel"
              value={model}
              onChange={(e) => setModel(e.target.value)}
              className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3 text-sm"
              disabled={loading}
            >
              {availableModels.map((m) => (
                <option key={m} value={m}>
                  {m}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Size Selection */}
        <div>
          <label
            htmlFor="imageSize"
            className="block text-sm font-medium text-gray-700"
          >
            Image Size
          </label>
          <select
            id="imageSize"
            value={size}
            onChange={(e) => setSize(e.target.value)}
            className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3 text-sm"
            disabled={loading}
          >
            {availableSizes.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <p className="mt-1 text-xs text-gray-500">
            Larger sizes take longer but have more detail
          </p>
        </div>

        {/* Quality Selection (DALL-E only) */}
        {provider === "dall-e" && (
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Quality
            </label>
            <div className="mt-2 flex items-center gap-4">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  value={0}
                  checked={quality === 0}
                  onChange={() => setQuality(0)}
                  disabled={loading}
                  className="w-4 h-4"
                />
                <span className="text-sm text-gray-700">Standard (Fast)</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  value={1}
                  checked={quality === 1}
                  onChange={() => setQuality(1)}
                  disabled={loading}
                  className="w-4 h-4"
                />
                <span className="text-sm text-gray-700">HD (Better Detail)</span>
              </label>
            </div>
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="rounded-2xl border border-red-200 bg-red-50 p-3 text-sm text-red-800">
            {error}
          </div>
        )}

        {/* Generate Button */}
        <button
          type="submit"
          disabled={loading || !prompt.trim() || providers.length === 0}
          className="w-full rounded-xl bg-slate-900 px-4 py-3 text-white font-medium hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
              Generating... (This may take 30-60 seconds)
            </span>
          ) : (
            "Generate Image"
          )}
        </button>
      </form>

      {/* Generated Image Display */}
      {imageUrl && (
        <div className="mt-6 space-y-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <img
              src={imageUrl}
              alt="Generated"
              className="w-full rounded-xl"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <a
              href={imageUrl}
              target="_blank"
              rel="noreferrer"
              className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-50"
            >
              Open in New Tab
            </a>
            <button
              type="button"
              onClick={() => {
                navigator.clipboard.writeText(imageUrl).catch(() => {
                  alert("Failed to copy URL");
                });
              }}
              className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-50"
            >
              Copy URL
            </button>
            {generationTime > 0 && (
              <span className="text-sm text-gray-600">
                Generated in {(generationTime / 1000).toFixed(1)}s
              </span>
            )}
          </div>

          <div className="rounded-2xl bg-slate-50 p-3 text-sm text-slate-700">
            <div className="font-semibold text-slate-900 mb-2">Your prompt:</div>
            <p className="text-slate-600">{prompt}</p>
          </div>
        </div>
      )}
    </div>
  );
}
