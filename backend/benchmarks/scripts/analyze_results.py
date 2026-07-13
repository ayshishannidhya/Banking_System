#!/usr/bin/env python3
"""
Statistical Analysis Script for IEEE Research Paper
====================================================
Project: Neptune Bank — Performance Evaluation of Event-Driven Microservices
Author: Ayshi Shannidhya Panda

This script analyzes JMeter/Gatling benchmark results and produces:
  - Descriptive statistics (mean, median, std dev, p95, p99)
  - 95% confidence intervals
  - One-way ANOVA across communication paradigms
  - Tukey's HSD post-hoc test
  - Shapiro-Wilk normality test
  - Kruskal-Wallis non-parametric test (if normality violated)
  - Cohen's d effect size
  - Publication-quality plots (LaTeX/PGFPlots compatible)

Usage:
  python analyze_results.py --input-dir ./results/ --output-dir ./analysis/

Input format:
  The script expects JMeter CSV results files named:
    {paradigm}_{scenario}_{users}_run{N}.csv
  Example: kafka_fund_transfer_5000_run1.csv

Dependencies:
  pip install pandas numpy scipy matplotlib seaborn scikit-posthocs
"""

import argparse
import glob
import os
import sys
from pathlib import Path

import matplotlib
matplotlib.use('Agg')  # Non-interactive backend for server environments

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from scipy import stats

# Optional: scikit-posthocs for Tukey's HSD
try:
    import scikit_posthocs as sp
    HAS_POSTHOCS = True
except ImportError:
    HAS_POSTHOCS = False
    print("WARNING: scikit-posthocs not installed. Tukey's HSD will be skipped.")
    print("Install with: pip install scikit-posthocs")


# ==============================================================================
# CONFIGURATION
# ==============================================================================

PARADIGMS = ['rest', 'rabbitmq', 'kafka']
USER_LEVELS = [100, 500, 1000, 5000, 10000, 20000]
ALPHA = 0.05  # Significance level
NUM_RUNS = 5  # Expected number of runs per experiment

# JMeter CSV columns of interest
JMETER_COLS = {
    'timeStamp': 'timestamp',
    'elapsed': 'response_time_ms',
    'label': 'label',
    'responseCode': 'response_code',
    'success': 'success',
    'bytes': 'bytes',
    'Latency': 'latency_ms',
    'Connect': 'connect_time_ms',
}

# Plot styling
plt.rcParams.update({
    'font.family': 'serif',
    'font.size': 10,
    'axes.labelsize': 11,
    'axes.titlesize': 12,
    'legend.fontsize': 9,
    'figure.figsize': (8, 5),
    'figure.dpi': 300,
    'savefig.dpi': 300,
    'savefig.bbox': 'tight',
})


# ==============================================================================
# STATISTICAL FUNCTIONS
# ==============================================================================

def compute_descriptive_stats(data: pd.Series) -> dict:
    """Compute descriptive statistics for a series of measurements."""
    n = len(data)
    mean = data.mean()
    median = data.median()
    std = data.std()
    p95 = data.quantile(0.95)
    p99 = data.quantile(0.99)

    # 95% confidence interval for the mean
    se = std / np.sqrt(n)
    ci_margin = stats.t.ppf(1 - ALPHA / 2, df=n - 1) * se
    ci_lower = mean - ci_margin
    ci_upper = mean + ci_margin

    return {
        'n': n,
        'mean': round(mean, 2),
        'median': round(median, 2),
        'std': round(std, 2),
        'p95': round(p95, 2),
        'p99': round(p99, 2),
        'ci_lower': round(ci_lower, 2),
        'ci_upper': round(ci_upper, 2),
        'ci_str': f"{round(mean, 2)} ± {round(ci_margin, 2)}",
    }


def cohens_d(group1: pd.Series, group2: pd.Series) -> float:
    """Compute Cohen's d effect size between two groups."""
    n1, n2 = len(group1), len(group2)
    var1, var2 = group1.var(), group2.var()
    pooled_std = np.sqrt(((n1 - 1) * var1 + (n2 - 1) * var2) / (n1 + n2 - 2))
    if pooled_std == 0:
        return 0.0
    return abs(group1.mean() - group2.mean()) / pooled_std


def interpret_cohens_d(d: float) -> str:
    """Interpret Cohen's d effect size."""
    if d < 0.2:
        return "negligible"
    elif d < 0.5:
        return "small"
    elif d < 0.8:
        return "medium"
    else:
        return "large"


def shapiro_wilk_test(data: pd.Series) -> dict:
    """Perform Shapiro-Wilk normality test."""
    if len(data) < 3:
        return {'statistic': None, 'p_value': None, 'normal': None}
    # Shapiro-Wilk works best with n <= 5000
    sample = data.sample(min(5000, len(data)), random_state=42)
    stat, p_value = stats.shapiro(sample)
    return {
        'statistic': round(stat, 4),
        'p_value': round(p_value, 6),
        'normal': p_value > ALPHA,
    }


def one_way_anova(*groups) -> dict:
    """Perform one-way ANOVA across groups."""
    f_stat, p_value = stats.f_oneway(*groups)
    return {
        'f_statistic': round(f_stat, 4),
        'p_value': round(p_value, 6),
        'significant': p_value < ALPHA,
    }


def kruskal_wallis_test(*groups) -> dict:
    """Non-parametric alternative to ANOVA."""
    h_stat, p_value = stats.kruskal(*groups)
    return {
        'h_statistic': round(h_stat, 4),
        'p_value': round(p_value, 6),
        'significant': p_value < ALPHA,
    }


# ==============================================================================
# DATA LOADING
# ==============================================================================

def load_jmeter_results(input_dir: str) -> pd.DataFrame:
    """Load and concatenate all JMeter CSV result files."""
    all_data = []
    csv_files = glob.glob(os.path.join(input_dir, '*.csv'))

    if not csv_files:
        print(f"ERROR: No CSV files found in {input_dir}")
        sys.exit(1)

    for csv_file in csv_files:
        filename = Path(csv_file).stem
        parts = filename.split('_')

        # Parse filename: {paradigm}_{scenario}_{users}_run{N}
        if len(parts) < 4:
            print(f"WARNING: Skipping {filename} — unexpected filename format")
            continue

        paradigm = parts[0]
        # Scenario may contain underscores, users and run are last two parts
        run_part = parts[-1]  # e.g., "run1"
        users_part = parts[-2]  # e.g., "5000"
        scenario = '_'.join(parts[1:-2])  # e.g., "fund_transfer"

        try:
            df = pd.read_csv(csv_file)
            df['paradigm'] = paradigm
            df['scenario'] = scenario
            df['users'] = int(users_part)
            df['run'] = int(run_part.replace('run', ''))
            all_data.append(df)
        except Exception as e:
            print(f"WARNING: Failed to load {csv_file}: {e}")

    if not all_data:
        print("ERROR: No valid data loaded")
        sys.exit(1)

    combined = pd.concat(all_data, ignore_index=True)
    print(f"Loaded {len(combined)} records from {len(csv_files)} files")
    return combined


# ==============================================================================
# ANALYSIS
# ==============================================================================

def analyze_throughput(data: pd.DataFrame, output_dir: str):
    """Analyze and compare throughput across paradigms and user levels."""
    print("\n" + "=" * 60)
    print("THROUGHPUT ANALYSIS")
    print("=" * 60)

    results = []

    for scenario in data['scenario'].unique():
        scenario_data = data[data['scenario'] == scenario]

        for users in sorted(scenario_data['users'].unique()):
            user_data = scenario_data[scenario_data['users'] == users]

            for paradigm in PARADIGMS:
                p_data = user_data[user_data['paradigm'] == paradigm]
                if p_data.empty:
                    continue

                # Calculate throughput per run
                throughputs = []
                for run in p_data['run'].unique():
                    run_data = p_data[p_data['run'] == run]
                    duration_s = (run_data['timeStamp'].max() - run_data['timeStamp'].min()) / 1000.0
                    if duration_s > 0:
                        throughputs.append(len(run_data) / duration_s)

                if throughputs:
                    stats_dict = compute_descriptive_stats(pd.Series(throughputs))
                    stats_dict.update({
                        'scenario': scenario,
                        'users': users,
                        'paradigm': paradigm,
                    })
                    results.append(stats_dict)

    results_df = pd.DataFrame(results)
    results_df.to_csv(os.path.join(output_dir, 'throughput_analysis.csv'), index=False)

    print(results_df.to_string(index=False))
    return results_df


def analyze_latency(data: pd.DataFrame, output_dir: str):
    """Analyze latency distributions across paradigms."""
    print("\n" + "=" * 60)
    print("LATENCY DISTRIBUTION ANALYSIS")
    print("=" * 60)

    results = []

    for scenario in data['scenario'].unique():
        for users in sorted(data[data['scenario'] == scenario]['users'].unique()):
            for paradigm in PARADIGMS:
                mask = (
                    (data['scenario'] == scenario) &
                    (data['users'] == users) &
                    (data['paradigm'] == paradigm)
                )
                p_data = data.loc[mask, 'elapsed']

                if p_data.empty:
                    continue

                stats_dict = compute_descriptive_stats(p_data)
                stats_dict.update({
                    'scenario': scenario,
                    'users': users,
                    'paradigm': paradigm,
                })
                results.append(stats_dict)

    results_df = pd.DataFrame(results)
    results_df.to_csv(os.path.join(output_dir, 'latency_analysis.csv'), index=False)
    print(results_df.to_string(index=False))
    return results_df


def perform_statistical_tests(data: pd.DataFrame, output_dir: str):
    """Perform ANOVA, Kruskal-Wallis, and effect size tests."""
    print("\n" + "=" * 60)
    print("STATISTICAL SIGNIFICANCE TESTS")
    print("=" * 60)

    results = []

    for scenario in data['scenario'].unique():
        for users in sorted(data[data['scenario'] == scenario]['users'].unique()):
            groups = {}
            for paradigm in PARADIGMS:
                mask = (
                    (data['scenario'] == scenario) &
                    (data['users'] == users) &
                    (data['paradigm'] == paradigm)
                )
                p_data = data.loc[mask, 'elapsed']
                if not p_data.empty:
                    groups[paradigm] = p_data

            if len(groups) < 2:
                continue

            group_list = list(groups.values())
            group_names = list(groups.keys())

            # Shapiro-Wilk normality test on each group
            normality = {}
            all_normal = True
            for name, group in groups.items():
                sw = shapiro_wilk_test(group)
                normality[name] = sw
                if sw['normal'] is False:
                    all_normal = False

            # ANOVA (parametric)
            anova = one_way_anova(*group_list)

            # Kruskal-Wallis (non-parametric)
            kw = kruskal_wallis_test(*group_list)

            # Pairwise Cohen's d
            pairwise_d = {}
            for i in range(len(group_names)):
                for j in range(i + 1, len(group_names)):
                    d = cohens_d(groups[group_names[i]], groups[group_names[j]])
                    pair_name = f"{group_names[i]}_vs_{group_names[j]}"
                    pairwise_d[pair_name] = {
                        'cohens_d': round(d, 4),
                        'interpretation': interpret_cohens_d(d),
                    }

            result = {
                'scenario': scenario,
                'users': users,
                'all_normal': all_normal,
                'anova_f': anova['f_statistic'],
                'anova_p': anova['p_value'],
                'anova_significant': anova['significant'],
                'kruskal_h': kw['h_statistic'],
                'kruskal_p': kw['p_value'],
                'kruskal_significant': kw['significant'],
                'recommended_test': 'ANOVA' if all_normal else 'Kruskal-Wallis',
            }

            # Add pairwise effect sizes
            for pair, effect in pairwise_d.items():
                result[f'd_{pair}'] = effect['cohens_d']
                result[f'effect_{pair}'] = effect['interpretation']

            results.append(result)

            # Print summary
            print(f"\n--- {scenario} | {users} users ---")
            print(f"  Normality (all groups): {'PASS' if all_normal else 'FAIL'}")
            print(f"  ANOVA: F={anova['f_statistic']}, p={anova['p_value']} "
                  f"{'*** SIGNIFICANT ***' if anova['significant'] else '(not significant)'}")
            print(f"  Kruskal-Wallis: H={kw['h_statistic']}, p={kw['p_value']} "
                  f"{'*** SIGNIFICANT ***' if kw['significant'] else '(not significant)'}")
            for pair, effect in pairwise_d.items():
                print(f"  Cohen's d ({pair}): {effect['cohens_d']} ({effect['interpretation']})")

    results_df = pd.DataFrame(results)
    results_df.to_csv(os.path.join(output_dir, 'statistical_tests.csv'), index=False)
    return results_df


# ==============================================================================
# PLOTTING
# ==============================================================================

def plot_throughput_comparison(throughput_df: pd.DataFrame, output_dir: str):
    """Generate throughput comparison bar chart."""
    if throughput_df.empty:
        return

    fig, ax = plt.subplots(figsize=(10, 6))

    scenarios = throughput_df['scenario'].unique()
    for scenario in scenarios:
        sdf = throughput_df[throughput_df['scenario'] == scenario]
        pivot = sdf.pivot(index='users', columns='paradigm', values='mean')

        x = np.arange(len(pivot.index))
        width = 0.25

        for i, paradigm in enumerate(PARADIGMS):
            if paradigm in pivot.columns:
                ax.bar(x + i * width, pivot[paradigm], width,
                       label=paradigm.upper() if scenario == scenarios[0] else "",
                       alpha=0.85)

        ax.set_xlabel('Concurrent Users')
        ax.set_ylabel('Throughput (req/s)')
        ax.set_title(f'Throughput Comparison — {scenario.replace("_", " ").title()}')
        ax.set_xticks(x + width)
        ax.set_xticklabels(pivot.index)
        ax.legend()
        ax.grid(axis='y', alpha=0.3)

        plt.savefig(os.path.join(output_dir, f'throughput_{scenario}.png'))
        plt.savefig(os.path.join(output_dir, f'throughput_{scenario}.pdf'))
        plt.clf()

    plt.close(fig)


def plot_latency_distribution(data: pd.DataFrame, output_dir: str):
    """Generate latency distribution box plots."""
    scenarios = data['scenario'].unique()

    for scenario in scenarios:
        for users in sorted(data[data['scenario'] == scenario]['users'].unique()):
            mask = (data['scenario'] == scenario) & (data['users'] == users)
            plot_data = data.loc[mask, ['paradigm', 'elapsed']].copy()

            if plot_data.empty:
                continue

            fig, ax = plt.subplots(figsize=(8, 5))
            sns.boxplot(data=plot_data, x='paradigm', y='elapsed',
                        order=PARADIGMS, palette='Set2', ax=ax,
                        showfliers=False)
            ax.set_xlabel('Communication Paradigm')
            ax.set_ylabel('Response Time (ms)')
            ax.set_title(f'Latency Distribution — {scenario.replace("_", " ").title()} | {users} Users')
            ax.grid(axis='y', alpha=0.3)

            plt.savefig(os.path.join(output_dir, f'latency_{scenario}_{users}.png'))
            plt.savefig(os.path.join(output_dir, f'latency_{scenario}_{users}.pdf'))
            plt.close(fig)


def plot_scalability_curve(throughput_df: pd.DataFrame, output_dir: str):
    """Generate scalability curves for each paradigm."""
    if throughput_df.empty:
        return

    for scenario in throughput_df['scenario'].unique():
        sdf = throughput_df[throughput_df['scenario'] == scenario]

        fig, ax = plt.subplots(figsize=(8, 5))
        colors = {'rest': '#e74c3c', 'rabbitmq': '#f39c12', 'kafka': '#2ecc71'}
        markers = {'rest': 'o', 'rabbitmq': 's', 'kafka': '^'}

        for paradigm in PARADIGMS:
            pdf = sdf[sdf['paradigm'] == paradigm].sort_values('users')
            if not pdf.empty:
                ax.plot(pdf['users'], pdf['mean'],
                        marker=markers.get(paradigm, 'o'),
                        color=colors.get(paradigm, 'blue'),
                        label=paradigm.upper(),
                        linewidth=2, markersize=8)
                # Confidence interval shading
                ax.fill_between(pdf['users'], pdf['ci_lower'], pdf['ci_upper'],
                                alpha=0.15, color=colors.get(paradigm, 'blue'))

        ax.set_xlabel('Concurrent Users')
        ax.set_ylabel('Throughput (req/s)')
        ax.set_title(f'Scalability Curve — {scenario.replace("_", " ").title()}')
        ax.legend()
        ax.grid(alpha=0.3)
        ax.set_xscale('log')

        plt.savefig(os.path.join(output_dir, f'scalability_{scenario}.png'))
        plt.savefig(os.path.join(output_dir, f'scalability_{scenario}.pdf'))
        plt.close(fig)


# ==============================================================================
# LATEX TABLE GENERATION
# ==============================================================================

def generate_latex_tables(throughput_df: pd.DataFrame, latency_df: pd.DataFrame, output_dir: str):
    """Generate LaTeX tables for direct inclusion in the IEEE paper."""

    # Throughput table
    if not throughput_df.empty:
        with open(os.path.join(output_dir, 'table_throughput.tex'), 'w') as f:
            f.write("% Auto-generated by analyze_results.py\n")
            f.write("% DO NOT EDIT — regenerate from raw data\n")
            f.write("\\begin{table}[!t]\n")
            f.write("\\caption{Throughput (req/s) Under Fund Transfer Workload --- Mean $\\pm$ 95\\% CI over 5 runs}\n")
            f.write("\\label{tab:throughput_results}\n")
            f.write("\\centering\n\\scriptsize\n")
            f.write("\\begin{tabularx}{\\columnwidth}{rXXX}\n\\toprule\n")
            f.write("\\textbf{Users} & \\textbf{REST} & \\textbf{RabbitMQ} & \\textbf{Kafka} \\\\\n\\midrule\n")

            for users in sorted(throughput_df['users'].unique()):
                row = [f"{users:,}"]
                for paradigm in PARADIGMS:
                    mask = (throughput_df['users'] == users) & (throughput_df['paradigm'] == paradigm)
                    subset = throughput_df[mask]
                    if not subset.empty:
                        row.append(subset.iloc[0]['ci_str'])
                    else:
                        row.append("--")
                f.write(" & ".join(row) + " \\\\\n")

            f.write("\\bottomrule\n\\end{tabularx}\n\\end{table}\n")

    print(f"\nLaTeX tables written to {output_dir}")


# ==============================================================================
# MAIN
# ==============================================================================

def main():
    parser = argparse.ArgumentParser(
        description='Statistical analysis for Neptune Bank IEEE research paper')
    parser.add_argument('--input-dir', required=True,
                        help='Directory containing JMeter CSV results')
    parser.add_argument('--output-dir', required=True,
                        help='Directory for analysis output (CSVs, plots, LaTeX)')

    args = parser.parse_args()

    os.makedirs(args.output_dir, exist_ok=True)

    # Load data
    data = load_jmeter_results(args.input_dir)

    # Run analyses
    throughput_df = analyze_throughput(data, args.output_dir)
    latency_df = analyze_latency(data, args.output_dir)
    stats_df = perform_statistical_tests(data, args.output_dir)

    # Generate plots
    plot_throughput_comparison(throughput_df, args.output_dir)
    plot_latency_distribution(data, args.output_dir)
    plot_scalability_curve(throughput_df, args.output_dir)

    # Generate LaTeX tables
    generate_latex_tables(throughput_df, latency_df, args.output_dir)

    print("\n" + "=" * 60)
    print("ANALYSIS COMPLETE")
    print(f"Results saved to: {args.output_dir}")
    print("=" * 60)


if __name__ == '__main__':
    main()
